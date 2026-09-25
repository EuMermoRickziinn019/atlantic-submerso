package com.submarino.specialArchives.impl;

import com.submarino.specialArchives.FormatoCriptografico;
import com.submarino.specialArchives.MaterialCriptografico;
import com.submarino.specialArchives.SubmarinoSpecialArchivesUtils;
import com.submarino.specialArchives.SenhaCriptograficaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class SubmarinoSpecialArchivesUtilsImpl
        implements SubmarinoSpecialArchivesUtils {

    @Override
    public MaterialCriptografico carregar(Path arquivo, FormatoCriptografico formato,
                                         char[] senha)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(arquivo, "Arquivo obrigatório");
        Objects.requireNonNull(formato, "Formato obrigatório");

        try (InputStream entrada = Files.newInputStream(arquivo)) {
            return carregar(entrada, formato, senha);
        }
    }

    @Override
    public MaterialCriptografico carregar(InputStream entrada, FormatoCriptografico formato,
                                         char[] senha)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(entrada, "Entrada obrigatória");
        Objects.requireNonNull(formato, "Formato obrigatório");
        char[] copia = senha == null ? null : senha.clone();
        try {
            return switch (formato) {
                case PKCS12 -> carregarRepositorio(entrada, "PKCS12", copia);
                case JKS -> carregarRepositorio(entrada, "JKS", copia);
                case X509 -> carregarCertificados(entrada);
                case PEM -> LeitorPem.carregar(entrada, copia);
            };
        } finally {
            if (copia != null) Arrays.fill(copia, '\0');
        }
    }

    private MaterialCriptografico carregarRepositorio(InputStream entrada,
                                                      String tipo, char[] senha)
            throws IOException, GeneralSecurityException {
        if (senha == null) {
            throw new SenhaCriptograficaException("Informe a senha; use char[0] para senha vazia");
        }
        KeyStore keyStore = KeyStore.getInstance(tipo);
        try {
            keyStore.load(entrada, senha);
        } catch (IOException e) {
            if (e.getCause() instanceof UnrecoverableKeyException) {
                throw new SenhaCriptograficaException("Senha inválida ou repositório corrompido", e);
            }
            throw e;
        }
        return new MaterialCriptografico.Repositorio(keyStore);
    }

    private MaterialCriptografico carregarCertificados(InputStream entrada)
            throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        var certificados = new ArrayList<X509Certificate>();

        for (var certificado : factory.generateCertificates(entrada)) {
            if (!(certificado instanceof X509Certificate x509)) {
                throw new CertificateException("Certificado encontrado não é X.509");
            }
            certificados.add(x509);
        }

        if (certificados.isEmpty()) {
            throw new CertificateException("O arquivo não contém certificados X.509");
        }
        return new MaterialCriptografico.Certificados(certificados);
    }
}
