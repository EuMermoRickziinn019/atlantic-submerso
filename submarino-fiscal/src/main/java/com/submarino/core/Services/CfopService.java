package com.submarino.core.Services;

import com.submarino.models.enums.NaturezaOperacao;
import com.submarino.models.enums.OperacaoOrigemDestino;
import com.submarino.models.enums.SituacaoContribuinteICMS;
import com.submarino.models.records.CFOP;

public class CfopService {

    public CFOP determinar(
            NaturezaOperacao naturezaOperacao,
            OperacaoOrigemDestino operacaoOrigemDestino,
            SituacaoContribuinteICMS situacaoContribuinteICMS
    ) {
        if (naturezaOperacao == null
                || operacaoOrigemDestino == null
                || situacaoContribuinteICMS == null) {
            throw new IllegalArgumentException(
                    "Natureza da operação, origem/destino e situação do contribuinte são obrigatórios"
            );
        }
        return switch (naturezaOperacao) {
            case VENDA_PRODUCAO_ESTABELECIMENTO ->
                    determinarVendaProducao(
                            operacaoOrigemDestino,
                            situacaoContribuinteICMS
                    );
            case VENDA_MERCADORIA_ADQUIRIDA ->
                    determinarVendaMercadoriaAdquirida(
                            operacaoOrigemDestino,
                            situacaoContribuinteICMS
                    );
            case VENDA_MERCADORIA_SUJEITA_ICMS_ST ->
                    determinarVendaMercadoriaST(
                            operacaoOrigemDestino
                    );
            case VENDA_ENTREGA_FUTURA ->
                    determinarVendaEntregaFutura(
                            operacaoOrigemDestino
                    );
            case REMESSA_INDUSTRIALIZACAO_ENCOMENDA ->
                    determinarRemessaIndustrializacao(
                            operacaoOrigemDestino
                    );
            case RETORNO_INDUSTRIALIZACAO ->
                    determinarRetornoIndustrializacao(
                            operacaoOrigemDestino
                    );
            case REMESSA_PARA_REPARO ->
                    determinarRemessaReparo(
                            operacaoOrigemDestino
                    );
            case RETORNO_REPARO ->
                    determinarRetornoReparo(
                            operacaoOrigemDestino
                    );
            case REMESSA_DEMONSTRACAO ->
                    determinarRemessaDemonstracao(
                            operacaoOrigemDestino
                    );
            case RETORNO_DEMONSTRACAO ->
                    determinarRetornoDemonstracao(
                            operacaoOrigemDestino
                    );
            case REMESSA_MOSTRUARIO ->
                    determinarRemessaMostruario(
                            operacaoOrigemDestino
                    );
            case RETORNO_MOSTRUARIO ->
                    determinarRetornoMostruario(
                            operacaoOrigemDestino
                    );
            case REMESSA_ARMAZEM_GERAL ->
                    determinarRemessaArmazem(
                            operacaoOrigemDestino
                    );
            case RETORNO_ARMAZEM_GERAL ->
                    determinarRetornoArmazem(
                            operacaoOrigemDestino
                    );
            case BRINDE_DOACAO ->
                    determinarBrindeDoacao(
                            operacaoOrigemDestino
                    );
            case AMOSTRA_GRATIS ->
                    determinarAmostraGratis(
                            operacaoOrigemDestino
                    );
            case DEVOLUCAO_COMPRA ->
                    throw requerInformacaoAdicional(
                            "DEVOLUCAO_COMPRA",
                            "É necessário saber se a compra era para COMERCIALIZACAO ou INDUSTRIALIZACAO."
                    );
            case DEVOLUCAO_VENDA ->
                    throw requerInformacaoAdicional(
                            "DEVOLUCAO_VENDA",
                            "É necessário saber se a mercadoria devolvida era produção própria ou mercadoria adquirida de terceiros."
                    );
            case RETORNO_MERCADORIA_NAO_ENTREGUE ->
                    throw requerInformacaoAdicional(
                            "RETORNO_MERCADORIA_NAO_ENTREGUE",
                            "O retorno deve considerar a operação original e a forma de emissão da NF-e."
                    );
            case TRANSFERENCIA_MERCADORIA ->
                    throw requerInformacaoAdicional(
                            "TRANSFERENCIA_MERCADORIA",
                            "É necessário saber se a mercadoria é produção própria ou adquirida de terceiros."
                    );
            case AJUSTE ->
                    throw requerInformacaoAdicional(
                            "AJUSTE",
                            "CFOP de ajuste depende do motivo específico do ajuste."
                    );
            case COMPLEMENTAR ->
                    throw requerInformacaoAdicional(
                            "COMPLEMENTAR",
                            "Nota complementar deve considerar o CFOP da operação original."
                    );
        };
    }
    private CFOP determinarVendaMercadoriaAdquirida(
            OperacaoOrigemDestino origemDestino,
            SituacaoContribuinteICMS contribuinte
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5102",
                            "Venda de mercadoria adquirida ou recebida de terceiros"
                    );
            case OPERACAO_INTERESTADUAL -> {
                if (contribuinte == SituacaoContribuinteICMS.NAO_CONTRIBUINTE) {
                    yield new CFOP(
                            "6108",
                            "Venda de mercadoria adquirida ou recebida de terceiros, destinada a não contribuinte"
                    );
                }
                yield new CFOP(
                        "6102",
                        "Venda de mercadoria adquirida ou recebida de terceiros"
                );
            }
            case OPERACAO_EXPORTACAO ->
                    new CFOP(
                            "7102",
                            "Venda de mercadoria adquirida ou recebida de terceiros"
                    );
            case OPERACAO_IMPORTACAO ->
                    throw operacaoInvalida(
                            "Importação não corresponde a uma operação de venda"
                    );
        };
    }
    private CFOP determinarVendaProducao(
            OperacaoOrigemDestino origemDestino,
            SituacaoContribuinteICMS contribuinte
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5101",
                            "Venda de produção do estabelecimento"
                    );
            case OPERACAO_INTERESTADUAL -> {
                if (contribuinte == SituacaoContribuinteICMS.NAO_CONTRIBUINTE) {
                    yield new CFOP(
                            "6107",
                            "Venda de produção do estabelecimento destinada a não contribuinte"
                    );
                }
                yield new CFOP(
                        "6101",
                        "Venda de produção do estabelecimento"
                );
            }
            case OPERACAO_EXPORTACAO ->
                    new CFOP(
                            "7101",
                            "Venda de produção do estabelecimento"
                    );
            case OPERACAO_IMPORTACAO ->
                    throw operacaoInvalida(
                            "Importação não corresponde a uma operação de venda"
                    );
        };
    }

    private CFOP determinarVendaMercadoriaST(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5405",
                            "Venda de mercadoria adquirida ou recebida de terceiros, sujeita à substituição tributária, na condição de contribuinte substituído"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6404",
                            "Venda de mercadoria sujeita à substituição tributária"
                    );
            default ->
                    throw operacaoInvalida(
                            "Venda com ICMS-ST precisa de classificação específica para esta operação"
                    );
        };
    }
    private CFOP determinarVendaEntregaFutura(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5922",
                            "Lançamento efetuado a título de simples faturamento decorrente de venda para entrega futura"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6922",
                            "Lançamento efetuado a título de simples faturamento decorrente de venda para entrega futura"
                    );
            default ->
                    throw operacaoInvalida(
                            "Entrega futura não implementada para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRemessaIndustrializacao(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5901",
                            "Remessa para industrialização por encomenda"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6901",
                            "Remessa para industrialização por encomenda"
                    );
            default ->
                    throw operacaoInvalida(
                            "Remessa para industrialização não implementada para esta origem/destino"
                    );
        };
    }

    private CFOP determinarRetornoIndustrializacao(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5902",
                            "Retorno de mercadoria utilizada na industrialização por encomenda"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6902",
                            "Retorno de mercadoria utilizada na industrialização por encomenda"
                    );
            default ->
                    throw operacaoInvalida(
                            "Retorno de industrialização não implementado para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRemessaReparo(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5915",
                            "Remessa de mercadoria ou bem para conserto ou reparo"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6915",
                            "Remessa de mercadoria ou bem para conserto ou reparo"
                    );
            default ->
                    throw operacaoInvalida(
                            "Remessa para reparo não implementada para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRetornoReparo(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5916",
                            "Retorno de mercadoria ou bem recebido para conserto ou reparo"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6916",
                            "Retorno de mercadoria ou bem recebido para conserto ou reparo"
                    );
            default ->
                    throw operacaoInvalida(
                            "Retorno de reparo não implementado para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRemessaDemonstracao(
            OperacaoOrigemDestino origemDestino
    ) {
        return determinarRemessaDemonstracaoOuMostruario(origemDestino);
    }

    private CFOP determinarRemessaMostruario(
            OperacaoOrigemDestino origemDestino
    ) {
        return determinarRemessaDemonstracaoOuMostruario(origemDestino);
    }

    private CFOP determinarRemessaDemonstracaoOuMostruario(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5912",
                            "Remessa de mercadoria ou bem para demonstração ou mostruário"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6912",
                            "Remessa de mercadoria ou bem para demonstração ou mostruário"
                    );
            default ->
                    throw operacaoInvalida(
                            "Remessa para demonstração/mostruário não implementada para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRetornoDemonstracao(
            OperacaoOrigemDestino origemDestino
    ) {
        return determinarRetornoDemonstracaoOuMostruario(origemDestino);
    }
    private CFOP determinarRetornoMostruario(
            OperacaoOrigemDestino origemDestino
    ) {
        return determinarRetornoDemonstracaoOuMostruario(origemDestino);
    }
    private CFOP determinarRetornoDemonstracaoOuMostruario(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5913",
                            "Retorno de mercadoria ou bem recebido para demonstração ou mostruário"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6913",
                            "Retorno de mercadoria ou bem recebido para demonstração ou mostruário"
                    );
            default ->
                    throw operacaoInvalida(
                            "Retorno de demonstração/mostruário não implementado para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRemessaArmazem(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5905",
                            "Remessa para depósito fechado ou armazém geral"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6905",
                            "Remessa para depósito fechado ou armazém geral"
                    );
            default ->
                    throw operacaoInvalida(
                            "Remessa para armazém não implementada para esta origem/destino"
                    );
        };
    }
    private CFOP determinarRetornoArmazem(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5906",
                            "Retorno de mercadoria depositada em depósito fechado ou armazém geral"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6906",
                            "Retorno de mercadoria depositada em depósito fechado ou armazém geral"
                    );
            default ->
                    throw operacaoInvalida(
                            "Retorno de armazém não implementado para esta origem/destino"
                    );
        };
    }
    private CFOP determinarBrindeDoacao(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5910",
                            "Remessa em bonificação, doação ou brinde"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6910",
                            "Remessa em bonificação, doação ou brinde"
                    );
            default ->
                    throw operacaoInvalida(
                            "Brinde/doação não implementado para esta origem/destino"
                    );
        };
    }
    private CFOP determinarAmostraGratis(
            OperacaoOrigemDestino origemDestino
    ) {
        return switch (origemDestino) {
            case OPERACAO_INTERNA ->
                    new CFOP(
                            "5911",
                            "Remessa de amostra grátis"
                    );
            case OPERACAO_INTERESTADUAL ->
                    new CFOP(
                            "6911",
                            "Remessa de amostra grátis"
                    );
            default ->
                    throw operacaoInvalida(
                            "Amostra grátis não implementada para esta origem/destino"
                    );
        };
    }
    private UnsupportedOperationException requerInformacaoAdicional(
            String operacao,
            String motivo
    ) {
        return new UnsupportedOperationException(
                "Não é possível determinar automaticamente o CFOP para "
                        + operacao + ". " + motivo
        );
    }

    private IllegalArgumentException operacaoInvalida(String mensagem) {
        return new IllegalArgumentException(mensagem);
    }
}