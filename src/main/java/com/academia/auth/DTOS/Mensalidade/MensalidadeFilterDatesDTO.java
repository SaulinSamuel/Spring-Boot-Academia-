package com.academia.auth.DTOS.Mensalidade;

import java.time.LocalDate;

public record MensalidadeFilterDatesDTO(
    LocalDate dataCriacaoInicio,
    LocalDate dataCriacaoFim,
    LocalDate dataPagamentoInicio,
    LocalDate dataPagamentoFim,
    LocalDate dataCancelamentoInicio,
    LocalDate dataCancelamentoFim,
    LocalDate dataVencimentoInicio,
    LocalDate dataVencimentoFim
) 
{
    
}
