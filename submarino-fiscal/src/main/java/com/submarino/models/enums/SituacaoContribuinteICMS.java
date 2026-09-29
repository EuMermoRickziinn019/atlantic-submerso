package com.submarino.models.enums;

public enum SituacaoContribuinteICMS  {
    CONTRIBUINTE(1),
    ISENTO(2),
    NAO_CONTRIBUINTE(9);

    private final int codigo;
    SituacaoContribuinteICMS(int codigo) {
        this.codigo = codigo;
    }
    public int getCodigo() {
        return codigo;
    }
}
