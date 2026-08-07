package com.academia.auth.DTOS.HistoricoAcessos;

import java.time.LocalDateTime;

import com.academia.auth.Models.enums.DiasSemana;
import com.academia.auth.Models.enums.RoleUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoAcessosResponseDTO {
    
    private Long id;

    private String usuario;
    
    private RoleUser role;

    private LocalDateTime horarioEntrada;

    private DiasSemana diaDaSemana;

}
