package com.submarino.core.Services;

import com.submarino.models.enums.EstadosBR;
import com.submarino.models.enums.OperacaoOrigemDestino;

public class OperacaoOrigemDestinoService {
    public OperacaoOrigemDestino determinar(
            EstadosBR ufEmitente,
            EstadosBR ufDestinatario
    ) {
        if(ufEmitente == null || ufDestinatario == null) {
            throw new IllegalArgumentException();
        }
        if(ufEmitente == ufDestinatario) {
            return OperacaoOrigemDestino.OPERACAO_INTERNA;
        }
        return OperacaoOrigemDestino.OPERACAO_INTERESTADUAL;
    }
}
