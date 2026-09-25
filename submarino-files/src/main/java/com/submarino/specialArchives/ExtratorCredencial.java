package com.submarino.specialArchives;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ExtratorCredencial {
    /** Lista aliases com chave; não escolhe arbitrariamente uma identidade. */
    public List<String> listarAliases(KeyStore repositorio) throws GeneralSecurityException {
        var resultado = new ArrayList<String>();
        var aliases = repositorio.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (repositorio.isKeyEntry(alias)
                    && repositorio.getCertificate(alias) instanceof X509Certificate) {
                resultado.add(alias);
            }
        }
        return List.copyOf(resultado);
    }

    /** A senha da entrada pode ser diferente da senha do arquivo. Para A3 já autenticado, null. */
    public CredencialAssinatura extrair(KeyStore repositorio, String alias, char[] senhaChave)
            throws GeneralSecurityException {
        Objects.requireNonNull(repositorio, "Repositório obrigatório");
        Objects.requireNonNull(alias, "Alias obrigatório");
        if (!repositorio.isKeyEntry(alias)) {
            throw new GeneralSecurityException("Alias não contém chave privada");
        }
        char[] copia = senhaChave == null ? null : senhaChave.clone();
        try {
            if (!(repositorio.getKey(alias, copia) instanceof PrivateKey chave)) {
                throw new GeneralSecurityException("Entrada não contém chave privada");
            }
            var cadeiaOriginal = repositorio.getCertificateChain(alias);
            if (cadeiaOriginal == null || cadeiaOriginal.length == 0) {
                throw new GeneralSecurityException("Entrada sem cadeia de certificados");
            }
            var cadeia = new ArrayList<X509Certificate>();
            for (var certificado : cadeiaOriginal) {
                if (!(certificado instanceof X509Certificate x509)) {
                    throw new GeneralSecurityException("Cadeia não é X.509");
                }
                cadeia.add(x509);
            }
            return new CredencialAssinatura(chave, cadeia,
                    "PKCS11".equalsIgnoreCase(repositorio.getType()) ? repositorio.getProvider() : null);
        } catch (UnrecoverableKeyException e) {
            throw new SenhaCriptograficaException("Senha da chave inválida ou entrada corrompida", e);
        } finally {
            if (copia != null) Arrays.fill(copia, '\0');
        }
    }
}
