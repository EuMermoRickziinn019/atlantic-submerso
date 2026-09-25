package com.submarino.specialArchives;

public enum FormatoCriptografico {
    PKCS12,
    JKS,
    /** Certificados X.509 em DER ou PEM; não inclui chaves privadas PEM. */
    X509,
    /** Certificados, chaves públicas e chaves privadas, inclusive criptografadas. */
    PEM
}
