package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Models.Mensalidade;

public class MensalidadeMapper {
    
    public static Mensalidade toEntity(MensalidadeRequestDTO dto) {

        Mensalidade mensalidade = new Mensalidade();
        
        mensalidade.setDiasTreino(dto.getDiasTreino());

        return mensalidade;
    }

    public static MensalidadeResponseDTO toDTO(Mensalidade mensalidade) {

        MensalidadeResponseDTO dto = new MensalidadeResponseDTO();

        dto.setDataCriacao(mensalidade.getDataCriacao());
        dto.setDataVencimento(mensalidade.getDataVencimento());
        dto.setDiasTreino(mensalidade.getDiasTreino());
        dto.setAluno(mensalidade.getUsuario().getNome());
        dto.setPreco(mensalidade.getValor());
        dto.setStatus(mensalidade.getStatus());
        dto.setId(mensalidade.getId());

        return dto;
    }
}
