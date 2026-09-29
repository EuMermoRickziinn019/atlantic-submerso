package com.submarino.core.Services;

import com.submarino.models.enums.RegimeTributario;
import com.submarino.models.records.ContextoFiscal;
import com.submarino.models.records.RegraIcms;
import com.submarino.models.records.TributacaoICMS;

public class TributacaoIcmsService {
    public TributacaoICMS determinar(
            ContextoFiscal contextoFiscal,
            RegraIcms regraIcms
    ) {
        validar(contextoFiscal, regraIcms);
        RegimeTributario regimeTributario =
                contextoFiscal.regimeTributario();
        return switch (regimeTributario) {
            case LUCRO_REAL,
                 LUCRO_PRESUMIDO ->
                    determinarCst(regraIcms);
            case SIMPLES_NACIONAL,
                 MEI ->
                    determinarCsosn(regraIcms);
        };
    }
    private TributacaoICMS determinarCst(
            RegraIcms regraIcms
    ) {
        if (regraIcms.cst() == null) {
            throw new IllegalStateException(
                    "A regra de ICMS não possui CST configurado"
            );
        }
        return new TributacaoICMS(
                "CST",
                regraIcms.cst().getCodigo(),
                regraIcms.cst().getDescricao()
        );
    }
    private TributacaoICMS determinarCsosn(
            RegraIcms regraIcms
    ) {
        if (regraIcms.csosn() == null) {
            throw new IllegalStateException(
                    "A regra de ICMS não possui CSOSN configurado"
            );
        }
        return new TributacaoICMS(
                "CSOSN",
                regraIcms.csosn().getCodigo(),
                regraIcms.csosn().getDescricao()
        );
    }
    private void validar(
            ContextoFiscal contextoFiscal,
            RegraIcms regraIcms
    ) {
        if (contextoFiscal == null) {
            throw new IllegalArgumentException(
                    "Contexto fiscal não pode ser nulo"
            );
        }
        if (regraIcms == null) {
            throw new IllegalArgumentException(
                    "Regra ICMS não pode ser nula"
            );
        }
        if (contextoFiscal.regimeTributario() == null) {
            throw new IllegalArgumentException(
                    "Regime tributário não pode ser nulo"
            );
        }
    }
}