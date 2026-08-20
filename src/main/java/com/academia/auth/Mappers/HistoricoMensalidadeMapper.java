package com.academia.auth.Mappers;

import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeResponseDTO;
import com.academia.auth.Models.HistoricoMensalidade;
import com.academia.auth.Models.Mensalidade;

public class HistoricoMensalidadeMapper {
    
    public static HistoricoMensalidade toHistoricoMensalidade(Mensalidade mensalidade) {

        HistoricoMensalidade historicoMensalidade = new HistoricoMensalidade();

        historicoMensalidade.setDataCancelamento(mensalidade.getDataCancelamento());
        historicoMensalidade.setDataCriacao(mensalidade.getDataCriacao());
        historicoMensalidade.setDataPagamento(mensalidade.getDataPagamento());
        historicoMensalidade.setDataVencimento(mensalidade.getDataVencimento());
        historicoMensalidade.setDiasTreino(mensalidade.getDiasTreino());
        historicoMensalidade.setNomeUsuario(mensalidade.getUsuario().getNome());
        historicoMensalidade.setStatus(mensalidade.getStatus());
        historicoMensalidade.setValor(mensalidade.getValor());

        return historicoMensalidade;
    }

    public static HistoricoMensalidadeResponseDTO toDTO(HistoricoMensalidade historicoMensalidade) {

        HistoricoMensalidadeResponseDTO dto = new HistoricoMensalidadeResponseDTO(
            historicoMensalidade.getNomeUsuario(),
            historicoMensalidade.getDiasTreino(),
            historicoMensalidade.getValor(),
            historicoMensalidade.getDataCriacao(),
            historicoMensalidade.getDataPagamento(),
            historicoMensalidade.getDataVencimento(),
            historicoMensalidade.getDataCancelamento(),
            historicoMensalidade.getStatus()
        );

        return dto;
    }

}
