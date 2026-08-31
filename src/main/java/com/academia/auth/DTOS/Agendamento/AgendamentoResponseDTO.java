package com.academia.auth.DTOS.Agendamento;

import com.academia.auth.Models.enums.StatusAgendamento;

public record AgendamentoResponseDTO(
    Long id,
    String nomeAula,
    String instrutor,
    String aluno,
    StatusAgendamento status
) {
    
}
