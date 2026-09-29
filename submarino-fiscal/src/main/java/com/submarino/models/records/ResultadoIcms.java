package com.submarino.models.records;

import java.math.BigDecimal;

public record ResultadoIcms(
        BigDecimal baseCalculo,
        BigDecimal aliquota,
        BigDecimal valorIcms,
        BigDecimal percentualDiferimento,
        BigDecimal valorIcmsDiferido,
        BigDecimal valorIcmsDevido
) {
}