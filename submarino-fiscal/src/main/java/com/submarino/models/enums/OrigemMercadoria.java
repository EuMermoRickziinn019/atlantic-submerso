package com.submarino.models.enums;

public enum OrigemMercadoria {
    //NACIONAL
    NACIONAL(0),
    NACIONAL_CONTEUDO_IMPORTACAO_SUP_40_INF_IGUAL_70(3),
    NACIONAL_CONTEUDO_IMPORTACAO_INF_IGUAL_40(5),
    NACIONAL_PPB(4),

    //IMPORTADOS
    ESTRANGEIRO_IMPOR_DIRETA(1),
    ESTRANGEIRO_ADQ_MERCADO_INTERNO(2),
    ESTRANGEIRO_IMPOR_DIRETA_SEM_SIMILAR_NACIONAL(6),
    ESTRANGEIRO_ADQ_MERCADO_INTERNO_SEM_SIMILAR_NACIONAL(7),
    NACIONAL_CONTEUDO_IMPORTACAO_SUP_70(8);

    private final Integer codigo;
    OrigemMercadoria(Integer codigo) {
        this.codigo = codigo;
    }
    public Integer getCodigo() {
        return codigo;
    }
}
