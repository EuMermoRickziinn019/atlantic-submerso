package com.submarino.specialArchives;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

/** Cadeia começa pelo certificado titular. Não armazena senha ou PIN. */
public record CredencialAssinatura(PrivateKey chavePrivada, List<X509Certificate> cadeia,
                                   Provider providerAssinatura) {
    public CredencialAssinatura(PrivateKey chavePrivada, List<X509Certificate> cadeia) {
        this(chavePrivada, cadeia, null);
    }
    public CredencialAssinatura {
        Objects.requireNonNull(chavePrivada, "Chave privada obrigatória");
        cadeia = List.copyOf(cadeia);
        if (cadeia.isEmpty()) throw new IllegalArgumentException("Cadeia obrigatória");
    }

    @Override
    public String toString() {
        return "CredencialAssinatura[certificados=" + cadeia.size() + "]";
    }
}
