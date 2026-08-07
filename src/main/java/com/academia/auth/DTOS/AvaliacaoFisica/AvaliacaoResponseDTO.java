package com.academia.auth.DTOS.AvaliacaoFisica;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoResponseDTO {
    
    private Long id;

    private Double altura;

    private Integer idade;

    private String nome;

    private Double percentualGordura;

    private Double massaMuscular;

    private Double braco;

    private Double peito;

    private Double cintura;

    private LocalDate dataAvaliacao;

}
