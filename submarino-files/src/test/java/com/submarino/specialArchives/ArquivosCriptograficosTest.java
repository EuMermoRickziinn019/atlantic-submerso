package com.submarino.specialArchives;

import com.submarino.specialArchives.impl.SubmarinoSpecialArchivesUtilsImpl;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.*;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArquivosCriptograficosTest {
    private static final BouncyCastleProvider BC = new BouncyCastleProvider();
    private static final Instant AGORA = Instant.parse("2026-09-24T12:00:00Z");
    private static KeyPair raizChaves;
    private static KeyPair chaves;
    private static X509Certificate raiz;
    private static X509Certificate certificado;
    private final SubmarinoSpecialArchivesUtils loader = new SubmarinoSpecialArchivesUtilsImpl();
    private final ValidadorCertificado validador = new ValidadorCertificado();
    @TempDir Path pasta;

    @BeforeAll
    static void preparar() throws Exception {
        var gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        raizChaves = gerador.generateKeyPair();
        chaves = gerador.generateKeyPair();
        raiz = emitir(raizChaves, true, BigInteger.ONE, AGORA.plusSeconds(86400), true);
        certificado = emitir(chaves, false, BigInteger.TWO, AGORA.plusSeconds(3600), true);
    }

    private static X509Certificate emitir(KeyPair par, boolean ca, BigInteger serial,
                                           Instant fim, boolean assinatura) throws Exception {
        var builder = new JcaX509v3CertificateBuilder(new X500Name("CN=Raiz de teste"), serial,
                Date.from(AGORA.minusSeconds(86400)), Date.from(fim),
                new X500Name(ca ? "CN=Raiz de teste" : "CN=Empresa teste"), par.getPublic());
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints,
                true, new BasicConstraints(ca));
        builder.addExtension(org.bouncycastle.asn1.x509.Extension.keyUsage, true,
                new KeyUsage(ca ? KeyUsage.keyCertSign | KeyUsage.cRLSign
                        : assinatura ? KeyUsage.digitalSignature : KeyUsage.keyEncipherment));
        if (!ca) {
            var nome = new OtherName(new ASN1ObjectIdentifier("2.16.76.1.3.3"),
                    new DEROctetString("12345678000190".getBytes(StandardCharsets.US_ASCII)));
            builder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectAlternativeName,
                    false, new GeneralNames(new GeneralName(GeneralName.otherName, nome)));
        }
        return new JcaX509CertificateConverter().setProvider(BC).getCertificate(builder.build(
                new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(raizChaves.getPrivate())));
    }

    private byte[] repositorio(FormatoCriptografico formato, char[] senha, char[] senhaChave)
            throws Exception {
        var ks = KeyStore.getInstance(formato.name());
        ks.load(null, senha);
        ks.setKeyEntry("empresa", chaves.getPrivate(), senhaChave,
                new java.security.cert.Certificate[]{certificado, raiz});
        var saida = new ByteArrayOutputStream();
        ks.store(saida, senha);
        return saida.toByteArray();
    }

    @ParameterizedTest
    @EnumSource(value = FormatoCriptografico.class, names = {"PKCS12", "JKS"})
    void carregaRepositorioEExtraiChave(FormatoCriptografico formato) throws Exception {
        char[] senha = "senha-teste".toCharArray();
        var arquivo = pasta.resolve("certificado.bin");
        Files.write(arquivo, repositorio(formato, senha, senha));
        var resultado = (MaterialCriptografico.Repositorio) loader.carregar(arquivo, formato, senha);
        var extrator = new ExtratorCredencial();
        assertEquals(List.of("empresa"), extrator.listarAliases(resultado.keyStore()));
        var credencial = extrator.extrair(resultado.keyStore(), "empresa", senha);
        validador.validarParaAssinatura(credencial, AGORA);
        assertArrayEquals("senha-teste".toCharArray(), senha);
        assertThrows(GeneralSecurityException.class,
                () -> extrator.extrair(resultado.keyStore(), "inexistente", senha));
    }

    @ParameterizedTest
    @EnumSource(value = FormatoCriptografico.class, names = {"PKCS12", "JKS"})
    void rejeitaSenhaErradaEAusente(FormatoCriptografico formato) throws Exception {
        byte[] bytes = repositorio(formato, "correta".toCharArray(), "correta".toCharArray());
        assertThrows(SenhaCriptograficaException.class, () -> loader.carregar(
                new ByteArrayInputStream(bytes), formato, "errada".toCharArray()));
        assertThrows(SenhaCriptograficaException.class, () -> loader.carregar(
                new ByteArrayInputStream(bytes), formato, null));
    }

    @Test
    void senhaDaChavePodeDiferirDaSenhaDoJks() throws Exception {
        var resultado = (MaterialCriptografico.Repositorio) loader.carregar(
                new ByteArrayInputStream(repositorio(FormatoCriptografico.JKS,
                        "arquivo".toCharArray(), "chave".toCharArray())),
                FormatoCriptografico.JKS, "arquivo".toCharArray());
        var extrator = new ExtratorCredencial();
        assertThrows(SenhaCriptograficaException.class,
                () -> extrator.extrair(resultado.keyStore(), "empresa", "arquivo".toCharArray()));
        validador.validarParaAssinatura(extrator.extrair(resultado.keyStore(),
                "empresa", "chave".toCharArray()), AGORA);
    }

    @Test
    void aceitaSenhaVaziaExplicita() throws Exception {
        loader.carregar(new ByteArrayInputStream(repositorio(FormatoCriptografico.PKCS12,
                new char[0], new char[0])), FormatoCriptografico.PKCS12, new char[0]);
    }

    private String pem(Object... objetos) throws Exception {
        var texto = new StringWriter();
        try (var writer = new JcaPEMWriter(texto)) {
            for (Object objeto : objetos) writer.writeObject(objeto);
        }
        return texto.toString();
    }

    private MaterialCriptografico lerPem(String texto, char[] senha) throws Exception {
        return loader.carregar(new ByteArrayInputStream(texto.getBytes(StandardCharsets.US_ASCII)),
                FormatoCriptografico.PEM, senha);
    }

    @Test
    void carregaPemComCertificadoChavePrivadaEPublica() throws Exception {
        var resultado = (MaterialCriptografico.Pem) lerPem(
                pem(certificado, chaves.getPrivate(), chaves.getPublic()), null);
        assertEquals(1, resultado.certificados().size());
        assertEquals(1, resultado.chavesPrivadas().size());
        assertFalse(resultado.chavesPublicas().isEmpty());
        validador.validarParaAssinatura(new CredencialAssinatura(
                resultado.chavesPrivadas().getFirst(), resultado.certificados()), AGORA);
    }

    @Test
    void carregaPkcs8CriptografadoERejeitaSenhaErrada() throws Exception {
        var encryptor = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC)
                .setProvider(BC).setPassword("segredo".toCharArray()).build();
        String conteudo = pem(new JcaPKCS8Generator(chaves.getPrivate(), encryptor));
        var resultado = (MaterialCriptografico.Pem) lerPem(conteudo, "segredo".toCharArray());
        assertEquals(1, resultado.chavesPrivadas().size());
        assertThrows(SenhaCriptograficaException.class, () -> lerPem(conteudo, null));
        assertThrows(SenhaCriptograficaException.class,
                () -> lerPem(conteudo, "errada".toCharArray()));
    }

    @Test
    void carregaPemTradicionalCriptografado() throws Exception {
        var texto = new StringWriter();
        try (var writer = new JcaPEMWriter(texto)) {
            writer.writeObject(chaves.getPrivate(), new JcePEMEncryptorBuilder("AES-256-CBC")
                    .setProvider(BC).build("segredo".toCharArray()));
        }
        assertEquals(1, ((MaterialCriptografico.Pem) lerPem(texto.toString(),
                "segredo".toCharArray())).chavesPrivadas().size());
        assertThrows(SenhaCriptograficaException.class,
                () -> lerPem(texto.toString(), "errada".toCharArray()));
    }

    @Test
    void carregaX509DerEPemSemFecharStreamDoChamador() throws Exception {
        class Entrada extends ByteArrayInputStream {
            boolean fechada;
            Entrada(byte[] bytes) { super(bytes); }
            @Override public void close() { fechada = true; }
        }
        for (byte[] bytes : List.of(certificado.getEncoded(),
                pem(certificado).getBytes(StandardCharsets.US_ASCII))) {
            var entrada = new Entrada(bytes);
            var resultado = (MaterialCriptografico.Certificados) loader.carregar(
                    entrada, FormatoCriptografico.X509, null);
            assertEquals(certificado, resultado.certificados().getFirst());
            assertFalse(entrada.fechada);
        }
    }

    @Test
    void rejeitaArquivoInvalidoOuAusente() {
        assertThrows(GeneralSecurityException.class, () -> lerPem("conteudo invalido", null));
        assertThrows(CertificateException.class, () -> loader.carregar(
                new ByteArrayInputStream(new byte[0]), FormatoCriptografico.X509, null));
        assertThrows(IOException.class, () -> loader.carregar(
                pasta.resolve("ausente"), FormatoCriptografico.PKCS12, new char[0]));
    }

    @Test
    void rejeitaCertificadoExpiradoChaveDivergenteEUsoIncorreto() throws Exception {
        var expirado = emitir(chaves, false, BigInteger.TEN, AGORA.minusSeconds(1), true);
        assertThrows(CertificateExpiredException.class, () -> validador.validarParaAssinatura(
                new CredencialAssinatura(chaves.getPrivate(), List.of(expirado)), AGORA));
        assertThrows(GeneralSecurityException.class, () -> validador.validarParaAssinatura(
                new CredencialAssinatura(raizChaves.getPrivate(), List.of(certificado)), AGORA));
        var semAssinatura = emitir(chaves, false, BigInteger.TEN, AGORA.plusSeconds(60), false);
        assertThrows(CertificateException.class, () -> validador.validarParaAssinatura(
                new CredencialAssinatura(chaves.getPrivate(), List.of(semAssinatura)), AGORA));
    }

    @Test
    void validaCnpjSemAceitarTitularDivergente() throws Exception {
        validador.validarCnpjTitular(certificado, "12.345.678/0001-90");
        assertThrows(CertificateException.class,
                () -> validador.validarCnpjTitular(certificado, "12345678000290"));
        assertThrows(CertificateException.class,
                () -> validador.validarCnpjTitular(raiz, "12345678000190"));
    }

    private X509CRL lcr(boolean revogado) throws Exception {
        var builder = new X509v2CRLBuilder(new X500Name("CN=Raiz de teste"),
                Date.from(AGORA.minusSeconds(60)));
        builder.setNextUpdate(Date.from(AGORA.plusSeconds(3600)));
        if (revogado) builder.addCRLEntry(certificado.getSerialNumber(),
                Date.from(AGORA.minusSeconds(30)), org.bouncycastle.asn1.x509.CRLReason.keyCompromise);
        return new JcaX509CRLConverter().setProvider(BC).getCRL(builder.build(
                new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(raizChaves.getPrivate())));
    }

    @Test
    void validaCadeiaERejeitaRevogadoOuSemProvaDeRevogacao() throws Exception {
        var credencial = new CredencialAssinatura(chaves.getPrivate(), List.of(certificado, raiz));
        var ancoras = Set.of(new TrustAnchor(raiz, null));
        assertNotNull(validador.validarConfianca(credencial, ancoras, List.of(lcr(false)), AGORA));
        assertThrows(GeneralSecurityException.class,
                () -> validador.validarConfianca(credencial, ancoras, List.of(lcr(true)), AGORA));
        assertThrows(CertificateException.class,
                () -> validador.validarConfianca(credencial, ancoras, List.of(), AGORA));
        assertThrows(CertificateException.class,
                () -> validador.validarConfianca(credencial, Set.of(), List.of(lcr(false)), AGORA));
    }

    @Test
    void a3ExigeConfiguracaoEPin() {
        assertThrows(SenhaCriptograficaException.class,
                () -> SessaoA3.abrir(pasta.resolve("token.cfg"), null));
        assertThrows(IOException.class,
                () -> SessaoA3.abrir(pasta.resolve("token.cfg"), "pin-teste".toCharArray()));
    }

    @Test
    void rejeitaCertificadoAindaNaoVigente() {
        var credencial = new CredencialAssinatura(chaves.getPrivate(), List.of(certificado));
        assertThrows(CertificateNotYetValidException.class,
                () -> validador.validarParaAssinatura(credencial, AGORA.minusSeconds(172800)));
    }

    @Test
    void rejeitaAutoridadeComoCertificadoTitular() {
        assertThrows(CertificateException.class, () -> validador.validarParaAssinatura(
                new CredencialAssinatura(raizChaves.getPrivate(), List.of(raiz)), AGORA));
    }

    @Test
    void rejeitaLcrExpiradaMesmoComCertificadoVigente() throws Exception {
        var builder = new X509v2CRLBuilder(new X500Name("CN=Raiz de teste"),
                Date.from(AGORA.minusSeconds(3600)));
        builder.setNextUpdate(Date.from(AGORA.minusSeconds(1800)));
        var expirada = new JcaX509CRLConverter().setProvider(BC).getCRL(builder.build(
                new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC).build(raizChaves.getPrivate())));
        assertThrows(GeneralSecurityException.class, () -> validador.validarConfianca(
                new CredencialAssinatura(chaves.getPrivate(), List.of(certificado, raiz)),
                Set.of(new TrustAnchor(raiz, null)), List.of(expirada), AGORA));
    }

    @Test
    void naoListaCertificadoPublicoComoChavePrivada() throws Exception {
        var ks = KeyStore.getInstance("JKS");
        ks.load(null, new char[0]);
        ks.setCertificateEntry("publico", certificado);
        var extrator = new ExtratorCredencial();
        assertTrue(extrator.listarAliases(ks).isEmpty());
        assertThrows(GeneralSecurityException.class,
                () -> extrator.extrair(ks, "publico", new char[0]));
    }

    @Test
    void retornaTodosOsAliasesSemEscolherUmaIdentidade() throws Exception {
        char[] senha = "teste".toCharArray();
        var ks = KeyStore.getInstance("JKS");
        ks.load(null, senha);
        ks.setKeyEntry("empresa1", chaves.getPrivate(), senha,
                new java.security.cert.Certificate[]{certificado, raiz});
        ks.setKeyEntry("empresa2", chaves.getPrivate(), senha,
                new java.security.cert.Certificate[]{certificado, raiz});
        assertEquals(Set.of("empresa1", "empresa2"),
                new HashSet<>(new ExtratorCredencial().listarAliases(ks)));
    }

    @Test
    void falhaDeSenhaNaoModificaArrayDoChamador() throws Exception {
        char[] senha = "errada".toCharArray();
        byte[] arquivo = repositorio(FormatoCriptografico.PKCS12,
                "correta".toCharArray(), "correta".toCharArray());
        assertThrows(SenhaCriptograficaException.class, () -> loader.carregar(
                new ByteArrayInputStream(arquivo), FormatoCriptografico.PKCS12, senha));
        assertArrayEquals("errada".toCharArray(), senha);
    }

    @Test
    void rejeitaBlocoPemQueNaoRepresentaCertificadoOuChave() throws Exception {
        String conteudo = pem(lcr(false));
        assertThrows(GeneralSecurityException.class, () -> lerPem(conteudo, null));
    }
}
