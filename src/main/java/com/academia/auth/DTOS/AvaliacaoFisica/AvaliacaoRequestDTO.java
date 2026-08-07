package com.academia.auth.DTOS.AvaliacaoFisica;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoRequestDTO {
    
    @NotNull
    private Double peso;

    @NotNull
    private Double altura;

    @NotNull
    private Integer idade;

    @NotNull
    private Double percentualGordura;

    @NotNull
    private Double massaMuscular;

    @NotNull
    private Double braco;

    @NotNull
    private Double peito;

    @NotNull
    private Double cintura;

}
