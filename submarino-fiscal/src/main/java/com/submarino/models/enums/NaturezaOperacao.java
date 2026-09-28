package com.submarino.models.enums;

public enum NaturezaOperacao {
        // Venda
        VENDA_PRODUCAO_ESTABELECIMENTO,
        VENDA_MERCADORIA_ADQUIRIDA,
        VENDA_MERCADORIA_SUJEITA_ICMS_ST,
        VENDA_ENTREGA_FUTURA,

        // Devolução / retorno
        DEVOLUCAO_COMPRA,
        DEVOLUCAO_VENDA,
        RETORNO_MERCADORIA_NAO_ENTREGUE,

        // Industrialização / reparo
        REMESSA_INDUSTRIALIZACAO_ENCOMENDA,
        RETORNO_INDUSTRIALIZACAO,
        REMESSA_PARA_REPARO,
        RETORNO_REPARO,

        // Logística
        REMESSA_DEMONSTRACAO,
        RETORNO_DEMONSTRACAO,
        REMESSA_MOSTRUARIO,
        RETORNO_MOSTRUARIO,
        REMESSA_ARMAZEM_GERAL,
        RETORNO_ARMAZEM_GERAL,
        TRANSFERENCIA_MERCADORIA,

        // Outras
        BRINDE_DOACAO,
        AMOSTRA_GRATIS,
        AJUSTE,
        COMPLEMENTAR;
}
