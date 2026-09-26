package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Models.Mensalidade;

public class MensalidadeMapper {

    public static MensalidadeResponseDTO toDTO(Mensalidade mensalidade) {

        MensalidadeResponseDTO dto = new MensalidadeResponseDTO();

        dto.setDataCriacao(mensalidade.getDataCriacao());
        dto.setDataVencimento(mensalidade.getDataVencimento());
        dto.setDataPagamento(mensalidade.getDataPagamento());
        dto.setDataCancelamento(mensalidade.getDataCancelamento());
        dto.setDiasTreino(mensalidade.getDiasTreino());
        dto.setAluno(mensalidade.getUsuario().getNome());
        dto.setPreco(mensalidade.getValor());
        dto.setStatus(mensalidade.getStatus());
        dto.setId(mensalidade.getId());

        return dto;
    }
}
