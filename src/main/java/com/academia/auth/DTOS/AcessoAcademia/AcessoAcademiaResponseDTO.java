package com.academia.auth.DTOS.AcessoAcademia;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcessoAcademiaResponseDTO {
    
    private String usuario;

    private Integer diasAcesso;

    private LocalDate inicioSemana;

    private LocalDate ultimoAcesso;
}
