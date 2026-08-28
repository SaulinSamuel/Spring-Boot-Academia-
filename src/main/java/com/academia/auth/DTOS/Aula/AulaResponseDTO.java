package com.academia.auth.DTOS.Aula;

import java.time.LocalDate;
import java.time.LocalTime;

import com.academia.auth.Models.enums.StatusAula;

import lombok.Builder;

@Builder
public record AulaResponseDTO(
    Long id,

    String nome,

    LocalDate dataAula,

    LocalTime horarioInicio,

    LocalTime horarioFim,

    Integer capacidadeInscricoes,

    StatusAula status,

    String instrutor
) {
    
}
