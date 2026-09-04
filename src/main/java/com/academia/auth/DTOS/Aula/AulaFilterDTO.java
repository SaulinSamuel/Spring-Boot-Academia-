package com.academia.auth.DTOS.Aula;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public record AulaFilterDTO(
    
    String nome,

    String nomeInstrutor,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataAulaInicio,
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataAulaFim,
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    Integer capacidadeInscricoes
) {
    
}
