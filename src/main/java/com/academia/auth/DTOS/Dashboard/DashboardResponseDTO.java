package com.academia.auth.DTOS.Dashboard;

import java.math.BigDecimal;

public record DashboardResponseDTO(

    Long quantidadeAlunos,

    Long mensalidadesPendentes,

    Long mensalidadesPagas,

    Long mensalidadesCanceladas,

    BigDecimal faturamentoTotal,

    Long quantidadeFuncionarios,

    Long acessosSemana,

    Double mediaDiasDeAcesso

)
{

}
    

