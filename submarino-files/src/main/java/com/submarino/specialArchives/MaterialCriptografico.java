package com.submarino.specialArchives;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

public sealed interface MaterialCriptografico {
    record Pem(List<X509Certificate> certificados, List<PrivateKey> chavesPrivadas,
               List<PublicKey> chavesPublicas) implements MaterialCriptografico {
        public Pem {
            certificados = List.copyOf(certificados);
            chavesPrivadas = List.copyOf(chavesPrivadas);
            chavesPublicas = List.copyOf(chavesPublicas);
        }

        @Override
        public String toString() {
            return "Pem[certificados=" + certificados.size()
                    + ", chavesPrivadas=" + chavesPrivadas.size()
                    + ", chavesPublicas=" + chavesPublicas.size() + "]";
        }
    }
    record Repositorio(KeyStore keyStore) implements MaterialCriptografico {
        public Repositorio {
            Objects.requireNonNull(keyStore, "KeyStore obrigatório");
        }
    }

    record Certificados(List<X509Certificate> certificados)
            implements MaterialCriptografico {
        public Certificados {
            certificados = List.copyOf(certificados);
        }
    }
}
