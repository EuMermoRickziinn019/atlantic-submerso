package com.submarino.specialArchives;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.OtherName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.*;
import java.time.Instant;
import java.util.*;

public final class ValidadorCertificado {
    /** Não implica confiança ICP-Brasil nem aprovação por um autorizador fiscal. */
    public void validarParaAssinatura(CredencialAssinatura credencial, Instant instante)
            throws GeneralSecurityException {
        Objects.requireNonNull(credencial, "Credencial obrigatória");
        Objects.requireNonNull(instante, "Instante obrigatório");
        for (var certificado : credencial.cadeia()) {
            certificado.checkValidity(Date.from(instante));
        }
        var titular = credencial.cadeia().getFirst();
        if (titular.getBasicConstraints() >= 0) {
            throw new CertificateException("Certificado de autoridade não pode ser usado como titular");
        }
        boolean[] usos = titular.getKeyUsage();
        if (usos != null && (usos.length == 0 || !usos[0])) {
            throw new CertificateException("Certificado não permite assinatura digital");
        }
        String algoritmo = switch (titular.getPublicKey().getAlgorithm()) {
            case "RSA" -> "SHA256withRSA";
            case "EC" -> "SHA256withECDSA";
            case "DSA" -> "SHA256withDSA";
            case "Ed25519" -> "Ed25519";
            case "Ed448" -> "Ed448";
            default -> throw new GeneralSecurityException("Algoritmo de assinatura não suportado");
        };
        byte[] desafio = new byte[32];
        new SecureRandom().nextBytes(desafio);
        var assinador = credencial.providerAssinatura() == null
                ? Signature.getInstance(algoritmo)
                : Signature.getInstance(algoritmo, credencial.providerAssinatura());
        assinador.initSign(credencial.chavePrivada());
        assinador.update(desafio);
        byte[] assinatura = assinador.sign();
        var verificador = Signature.getInstance(algoritmo);
        verificador.initVerify(titular);
        verificador.update(desafio);
        if (!verificador.verify(assinatura)) {
            throw new CertificateException("Chave privada não corresponde ao certificado");
        }
    }

    /**
     * Constrói cadeia PKIX até raízes explicitamente confiáveis. Exige LCRs válidas
     * para os certificados da cadeia; ausência de prova de revogação causa falha.
     * Não transforma certificados importados automaticamente em raízes confiáveis.
     */
    public PKIXCertPathBuilderResult validarConfianca(CredencialAssinatura credencial,
            Set<TrustAnchor> raizesConfiaveis, Collection<X509CRL> lcrs, Instant instante)
            throws GeneralSecurityException {
        validarParaAssinatura(credencial, instante);
        if (raizesConfiaveis.isEmpty()) {
            throw new CertificateException("Informe as raízes confiáveis");
        }
        if (lcrs.isEmpty()) {
            throw new CertificateException("Informe LCRs atuais para verificar revogação");
        }
        var seletor = new X509CertSelector();
        seletor.setCertificate(credencial.cadeia().getFirst());
        var parametros = new PKIXBuilderParameters(Set.copyOf(raizesConfiaveis), seletor);
        parametros.setDate(Date.from(instante));
        parametros.setRevocationEnabled(true);
        var material = new ArrayList<Object>(credencial.cadeia());
        material.addAll(lcrs);
        parametros.addCertStore(CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(material)));
        var builder = CertPathBuilder.getInstance("PKIX");
        var revogacao = (PKIXRevocationChecker) builder.getRevocationChecker();
        revogacao.setOptions(EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS,
                PKIXRevocationChecker.Option.NO_FALLBACK));
        parametros.addCertPathChecker(revogacao);
        return (PKIXCertPathBuilderResult) builder.build(parametros);
    }

    /** Comparação exata de CNPJ do titular; não presume regras de matriz/filial ou procuração. */
    public void validarCnpjTitular(X509Certificate certificado, String cnpjEsperado)
            throws CertificateException {
        Objects.requireNonNull(cnpjEsperado, "CNPJ esperado obrigatório");
        String esperado = cnpjEsperado.replaceAll("[./\\-\\s]", "").toUpperCase(Locale.ROOT);
        if (!esperado.matches("[A-Z0-9]{12}[0-9]{2}")) {
            throw new IllegalArgumentException("Formato de CNPJ inválido");
        }
        var holder = new JcaX509CertificateHolder(certificado);
        var encontrados = new HashSet<String>();
        if (holder.getExtension(Extension.subjectAlternativeName) != null) {
            try {
                var nomes = GeneralNames.fromExtensions(holder.getExtensions(),
                        Extension.subjectAlternativeName).getNames();
                for (var nome : nomes) {
                    if (nome.getTagNo() != GeneralName.otherName) continue;
                    var other = OtherName.getInstance(nome.getName());
                    if (!"2.16.76.1.3.3".equals(other.getTypeID().getId())) continue;
                    var valor = other.getValue();
                    String cnpj;
                    if (valor instanceof ASN1String texto) {
                        cnpj = texto.getString();
                    } else if (valor instanceof ASN1OctetString octetos) {
                        cnpj = new String(octetos.getOctets(), StandardCharsets.US_ASCII);
                    } else {
                        throw new CertificateException("Codificação do CNPJ não suportada");
                    }
                    encontrados.add(cnpj);
                }
            } catch (IllegalArgumentException | IllegalStateException | ClassCastException e) {
                throw new CertificateException("Identidade CNPJ malformada", e);
            }
        }
        if (!encontrados.equals(Set.of(esperado))) {
            throw new CertificateException("CNPJ ausente, divergente ou ambíguo no certificado");
        }
    }
}
