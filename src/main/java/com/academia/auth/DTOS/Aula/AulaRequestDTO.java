package com.academia.auth.DTOS.Aula;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AulaRequestDTO(
    
    @NotBlank
    String nome,

    @NotNull
    LocalDate dataAula,

    @NotNull
    LocalTime horarioInicio,

    @NotNull
    LocalTime horarioFim,

    @NotNull
    Integer capacidadeInscricoes
) {
    
}
