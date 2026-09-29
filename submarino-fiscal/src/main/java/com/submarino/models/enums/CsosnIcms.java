package com.submarino.models.enums;

public enum CsosnIcms {

    CSOSN_101("101", "Tributada pelo Simples Nacional com permissão de crédito"),
    CSOSN_102("102", "Tributada pelo Simples Nacional sem permissão de crédito"),
    CSOSN_103("103", "Isenção do ICMS no Simples Nacional para faixa de receita bruta"),
    CSOSN_201("201", "Tributada pelo Simples Nacional com crédito e ICMS-ST"),
    CSOSN_202("202", "Tributada pelo Simples Nacional sem crédito e com ICMS-ST"),
    CSOSN_203("203", "Isenção no Simples Nacional e ICMS-ST"),
    CSOSN_300("300", "Imune"),
    CSOSN_400("400", "Não tributada pelo Simples Nacional"),
    CSOSN_500("500", "ICMS cobrado anteriormente por ST ou antecipação"),
    CSOSN_900("900", "Outros");

    private final String codigo;
    private final String descricao;

    CsosnIcms(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}