package com.academia.auth.DTOS.AcessoAcademia;

import java.time.LocalDate;

import com.academia.auth.Models.enums.RoleUser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcessoAcademiaResponseDTO {
    
    private String usuario;

    private RoleUser role;

    private Integer diasAcessoSemana;

    private LocalDate inicioSemana;

    private LocalDate ultimoAcesso;
}
