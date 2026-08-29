package com.academia.auth.DTOS.Aula;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AulaRequestDTO(
    
    @NotBlank
    String nome,

    @NotNull
    @Future
    LocalDate dataAula,

    @NotNull
    LocalTime horarioInicio,

    @NotNull
    LocalTime horarioFim,

    @NotNull
    @Min(0)
    @Max(50)
    Integer capacidadeInscricoes
) {
    
}
