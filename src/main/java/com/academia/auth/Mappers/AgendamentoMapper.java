package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Agendamento.AgendamentoResponseDTO;
import com.academia.auth.Models.Agendamento;

public class AgendamentoMapper {
    
    public static AgendamentoResponseDTO toDTO(Agendamento agendamento) {

        AgendamentoResponseDTO dto = new AgendamentoResponseDTO(
            agendamento.getId(),
            agendamento.getAula().getNome(),
            agendamento.getAula().getInstrutor().getNome(),
            agendamento.getUsuario().getNome(),
            agendamento.getStatus()    
        );

        return dto;
    }

}
