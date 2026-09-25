package com.submarino.specialArchives;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AuthProvider;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Objects;
import javax.security.auth.login.LoginException;

/** Sessão PKCS#11. Use try-with-resources; não compartilhe sessões do mesmo token. */
public final class SessaoA3 implements AutoCloseable {
    private final AuthProvider provider;
    private final KeyStore repositorio;
    private boolean fechada;

    private SessaoA3(AuthProvider provider, KeyStore repositorio) {
        this.provider = provider;
        this.repositorio = repositorio;
    }

    /** Configuração local confiável do SunPKCS11 com biblioteca nativa do fabricante. */
    public static SessaoA3 abrir(Path configuracao, char[] pin)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(configuracao, "Configuração obrigatória");
        if (pin == null) throw new SenhaCriptograficaException("PIN obrigatório");
        if (!Files.isRegularFile(configuracao)) {
            throw new IOException("Arquivo de configuração PKCS#11 não encontrado");
        }
        var base = Security.getProvider("SunPKCS11");
        if (base == null) throw new GeneralSecurityException("SunPKCS11 indisponível nesta JVM");
        AuthProvider configurado;
        try {
            var candidato = base.configure(configuracao.toAbsolutePath().toString());
            if (!(candidato instanceof AuthProvider autenticavel)) {
                throw new GeneralSecurityException("Provider não suporta autenticação");
            }
            configurado = autenticavel;
        } catch (RuntimeException e) {
            throw new GeneralSecurityException("Falha na configuração do driver PKCS#11", e);
        }
        char[] copia = pin.clone();
        try {
            var repositorio = KeyStore.getInstance("PKCS11", configurado);
            // Uma tentativa apenas: retries automáticos podem bloquear o token.
            repositorio.load(null, copia);
            return new SessaoA3(configurado, repositorio);
        } catch (IOException | GeneralSecurityException | RuntimeException e) {
            try { configurado.logout(); } catch (Exception limpeza) { e.addSuppressed(limpeza); }
            throw new GeneralSecurityException("Não foi possível abrir o token; confira PIN, dispositivo e driver", e);
        } finally {
            Arrays.fill(copia, '\0');
        }
    }

    public KeyStore repositorio() {
        if (fechada) throw new IllegalStateException("Sessão encerrada");
        return repositorio;
    }

    @Override
    public void close() throws LoginException {
        if (!fechada) {
            provider.logout();
            fechada = true;
        }
    }
}
