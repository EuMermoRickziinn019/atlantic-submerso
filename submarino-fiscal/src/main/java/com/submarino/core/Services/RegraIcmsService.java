package com.submarino.core.Services;

import com.submarino.models.records.CFOP;
import com.submarino.models.records.ContextoFiscal;
import com.submarino.models.records.RegraIcms;

import java.time.LocalDate;

public class RegraIcmsService {
    private final RegraIcmsRepository repository;
    public RegraIcmsService(
            RegraIcmsRepository repository
    ) {
        this.repository = repository;
    }
    public RegraIcms buscarRegra(
            ContextoFiscal contexto,
            CFOP cfop
    ) {
        if (contexto == null) {
            throw new IllegalArgumentException(
                    "Contexto fiscal não pode ser nulo"
            );
        }
        if (cfop == null) {
            throw new IllegalArgumentException(
                    "CFOP não pode ser nulo"
            );
        }
        LocalDate dataOperacao =
                contexto.dataOperacao();
        return repository.buscarTodas()
            .stream()
            .filter(regra ->
                    regra.ncm()
                            .equals(contexto.ncm())
            )
            .filter(regra ->
                    regra.ufOrigem()
                            == contexto.ufEmitente()
            )
            .filter(regra ->
                    regra.ufDestino()
                            == contexto.ufDestinatario()
            )
            .filter(regra ->
                    regra.naturezaOperacao()
                            == contexto.naturezaOperacao()
            )
            .filter(regra ->
                    regra.situacaoContribuinteICMS()
                            == contexto.contribuinteICMS()
            )
            .filter(regra ->
                    regra.consumidorFinal()
                            == contexto.consumidorFinal()
            )
            .filter(regra ->
                    regraVigente(
                            regra,
                            dataOperacao
                    )
            )
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                "Nenhuma regra ICMS encontrada para:"
                    + " NCM=" + contexto.ncm()
                    + ", UF origem=" + contexto.ufEmitente()
                    + ", UF destino=" + contexto.ufDestinatario()
                    + ", natureza=" + contexto.naturezaOperacao()
                    + ", contribuinte=" + contexto.contribuinteICMS()
                    + ", consumidor final=" + contexto.consumidorFinal()
                    + ", CFOP=" + cfop.codigo()
                )
            );
    }
    private boolean regraVigente(
            RegraIcms regra,
            LocalDate data
    ) {
        boolean inicioValido =
                !data.isBefore(
                        regra.inicioVigencia()
                );
        boolean fimValido =
                regra.fimVigencia() == null
                        || !data.isAfter(
                        regra.fimVigencia()
                );
        return inicioValido && fimValido;
    }
}