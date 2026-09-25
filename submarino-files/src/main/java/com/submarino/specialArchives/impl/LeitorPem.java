package com.submarino.specialArchives.impl;

import com.submarino.specialArchives.MaterialCriptografico;
import com.submarino.specialArchives.SenhaCriptograficaException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

final class LeitorPem {
    private LeitorPem() {}

    static MaterialCriptografico carregar(InputStream entrada, char[] senha)
            throws IOException, GeneralSecurityException {
        var provider = new BouncyCastleProvider();
        var conversor = new JcaPEMKeyConverter().setProvider(provider);
        var certificados = new ArrayList<X509Certificate>();
        var privadas = new ArrayList<PrivateKey>();
        var publicas = new ArrayList<PublicKey>();
        // O stream externo pertence ao chamador e não deve ser fechado aqui.
        var parser = new PEMParser(new InputStreamReader(entrada, StandardCharsets.US_ASCII));
        Object objeto;
        while ((objeto = parser.readObject()) != null) {
            if (objeto instanceof PKCS8EncryptedPrivateKeyInfo criptografada) {
                exigirSenha(senha);
                try {
                    objeto = criptografada.decryptPrivateKeyInfo(
                            new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                    .setProvider(provider).build(senha));
                } catch (Exception e) {
                    throw new SenhaCriptograficaException(
                            "Não foi possível descriptografar PKCS8: verifique senha, algoritmo e integridade", e);
                }
            } else if (objeto instanceof PEMEncryptedKeyPair criptografada) {
                exigirSenha(senha);
                try {
                    objeto = criptografada.decryptKeyPair(
                            new JcePEMDecryptorProviderBuilder().setProvider(provider).build(senha));
                } catch (IOException e) {
                    throw new SenhaCriptograficaException(
                            "Não foi possível descriptografar PEM: verifique senha e integridade", e);
                }
            }
            if (objeto instanceof X509CertificateHolder certificado) {
                certificados.add(new JcaX509CertificateConverter().setProvider(provider)
                        .getCertificate(certificado));
            } else if (objeto instanceof PEMKeyPair par) {
                var chaves = conversor.getKeyPair(par);
                privadas.add(chaves.getPrivate());
                publicas.add(chaves.getPublic());
            } else if (objeto instanceof PrivateKeyInfo chave) {
                privadas.add(conversor.getPrivateKey(chave));
            } else if (objeto instanceof SubjectPublicKeyInfo chave) {
                publicas.add(conversor.getPublicKey(chave));
            } else {
                throw new GeneralSecurityException("Bloco PEM não suportado: "
                        + objeto.getClass().getSimpleName());
            }
        }
        if (certificados.isEmpty() && privadas.isEmpty() && publicas.isEmpty()) {
            throw new GeneralSecurityException("Nenhum material criptográfico PEM encontrado");
        }
        return new MaterialCriptografico.Pem(certificados, privadas, publicas);
    }

    private static void exigirSenha(char[] senha) throws SenhaCriptograficaException {
        if (senha == null) {
            throw new SenhaCriptograficaException("Senha obrigatória para chave criptografada");
        }
    }
}
