package com.submarino.models.records;

import java.math.BigDecimal;

public record ValoresOperacao(
    BigDecimal valorProdutos,
    BigDecimal frete,
    BigDecimal seguro,
    BigDecimal outrasDespesas,
    BigDecimal desconto
) {
}
