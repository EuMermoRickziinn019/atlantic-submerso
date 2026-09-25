package com.submarino.specialArchives;

import java.security.GeneralSecurityException;

/** Falha de autenticação ou integridade: conteúdo corrompido também pode causá-la. */
public final class SenhaCriptograficaException extends GeneralSecurityException {
    public SenhaCriptograficaException(String mensagem) {
        super(mensagem);
    }

    public SenhaCriptograficaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
