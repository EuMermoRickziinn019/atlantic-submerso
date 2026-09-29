package com.submarino.core.Services;

import com.submarino.models.records.RegraIcms;
import com.submarino.models.records.ResultadoIcms;
import com.submarino.models.records.ValoresOperacao;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraIcmsService {
    private static final BigDecimal CEM =
            new BigDecimal("100");
    public ResultadoIcms calcular(
            ValoresOperacao valores,
            RegraIcms regra
    ) {
        validar(valores, regra);
        BigDecimal baseCalculo =
                calcularBase(valores);
        if (regra.isento()) {
            return new ResultadoIcms(
                    baseCalculo,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
        if (regra.reducaoBase()) {
            baseCalculo =
                    aplicarReducaoBase(
                            baseCalculo,
                            regra.percentualReducaoBase()
                    );
        }
        BigDecimal valorIcms =
                calcularValorIcms(
                        baseCalculo,
                        regra.aliquota()
                );
        BigDecimal percentualDiferimento =
                BigDecimal.ZERO;
        BigDecimal valorIcmsDiferido =
                BigDecimal.ZERO;
        BigDecimal valorIcmsDevido =
                valorIcms;
        if (regra.diferimento()) {
            percentualDiferimento =
                    regra.percentualDiferimento();
            valorIcmsDiferido =
                    calcularPercentual(
                            valorIcms,
                            percentualDiferimento
                    );
            valorIcmsDevido =
                    valorIcms
                            .subtract(valorIcmsDiferido)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }
        return new ResultadoIcms(
                baseCalculo,
                regra.aliquota(),
                valorIcms,
                percentualDiferimento,
                valorIcmsDiferido,
                valorIcmsDevido
        );
    }

    private BigDecimal calcularBase(
            ValoresOperacao valores
    ) {
        return valorOuZero(valores.valorProdutos())
                .add(valorOuZero(valores.frete()))
                .add(valorOuZero(valores.seguro()))
                .add(valorOuZero(valores.outrasDespesas()))
                .subtract(valorOuZero(valores.desconto()))
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal aplicarReducaoBase(
            BigDecimal baseCalculo,
            BigDecimal percentualReducao
    ) {
        BigDecimal percentualRestante =
                CEM.subtract(percentualReducao);
        return baseCalculo
                .multiply(percentualRestante)
                .divide(
                        CEM,
                        2,
                        RoundingMode.HALF_UP
                );
    }
    private BigDecimal calcularValorIcms(
            BigDecimal baseCalculo,
            BigDecimal aliquota
    ) {
        return calcularPercentual(
                baseCalculo,
                aliquota
        );
    }
    private BigDecimal calcularPercentual(
            BigDecimal valor,
            BigDecimal percentual
    ) {
        return valor
                .multiply(percentual)
                .divide(
                        CEM,
                        2,
                        RoundingMode.HALF_UP
                );
    }
    private BigDecimal valorOuZero(
            BigDecimal valor
    ) {
        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }
    private void validar(
            ValoresOperacao valores,
            RegraIcms regra
    ) {
        if (valores == null) {
            throw new IllegalArgumentException(
                    "Valores da operação não podem ser nulos"
            );
        }
        if (regra == null) {
            throw new IllegalArgumentException(
                    "Regra ICMS não pode ser nula"
            );
        }
        if (valores.valorProdutos() == null) {
            throw new IllegalArgumentException(
                    "Valor dos produtos não pode ser nulo"
            );
        }
        if (!regra.isento()
                && regra.aliquota() == null) {

            throw new IllegalStateException(
                    "Regra ICMS não possui alíquota"
            );
        }
        if (regra.reducaoBase()) {
            if (regra.percentualReducaoBase() == null) {
                throw new IllegalStateException(
                        "Regra possui redução de base sem percentual configurado"
                );
            }
            validarPercentual(
                    regra.percentualReducaoBase(),
                    "Percentual de redução da base"
            );
        }
        if (regra.diferimento()) {

            if (regra.percentualDiferimento() == null) {
                throw new IllegalStateException(
                        "Regra possui diferimento sem percentual configurado"
                );
            }
            validarPercentual(
                    regra.percentualDiferimento(),
                    "Percentual de diferimento"
            );
        }
    }
    private void validarPercentual(
            BigDecimal percentual,
            String campo
    ) {
        if (percentual.compareTo(BigDecimal.ZERO) < 0
                || percentual.compareTo(CEM) > 0) {

            throw new IllegalArgumentException(
                    campo + " deve estar entre 0 e 100"
            );
        }
    }
}