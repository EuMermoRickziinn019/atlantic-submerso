package com.submarino.models.enums;

public enum IndicadorConsumidorFinal {
    NAO_CONSUMIDOR_FINAL(0),
    CONSUMIDOR_FINAL(1);

    private final int codigo;
    IndicadorConsumidorFinal(int codigo) {
        this.codigo = codigo;
    }
    public int getCodigo() {
        return codigo;
    }
}
