package com.academia.auth.DTOS.Dashboard;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDTO {
    
    private Long quantidadeAlunos;

    private Long mensalidadesPendentes;

    private Long mensalidadesPagas;

    private BigDecimal faturamentoTotal;

    private Long quantidadeFuncionarios;

    private Long acessosSemana;

}
