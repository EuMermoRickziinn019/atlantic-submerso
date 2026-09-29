package com.submarino.models.records;

import com.submarino.models.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegraIcms(
        String ncm,
        EstadosBR ufOrigem,
        EstadosBR ufDestino,
        NaturezaOperacao naturezaOperacao,
        SituacaoContribuinteICMS situacaoContribuinteICMS,
        IndicadorConsumidorFinal consumidorFinal,
        CstIcms cst,
        CsosnIcms csosn,
        boolean substituicaoTributaria,
        boolean isento,
        boolean reducaoBase,
        boolean diferimento,
        BigDecimal aliquota,
        BigDecimal percentualReducaoBase,
        BigDecimal percentualDiferimento,
        LocalDate inicioVigencia,
        LocalDate fimVigencia
) {
}
