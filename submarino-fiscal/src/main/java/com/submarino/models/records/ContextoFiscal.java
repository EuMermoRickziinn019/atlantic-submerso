package com.submarino.models.records;

import com.submarino.models.enums.*;

import java.time.LocalDate;

public record ContextoFiscal(
    RegimeTributario regimeTributario,
    EstadosBR ufEmitente,
    EstadosBR ufDestinatario,
    SituacaoContribuinteICMS contribuinteICMS,
    IndicadorConsumidorFinal consumidorFinal,
    NaturezaOperacao naturezaOperacao,
    OrigemMercadoria origemMercadoria,
    String ncm,
    LocalDate dataOperacao
) {
}
