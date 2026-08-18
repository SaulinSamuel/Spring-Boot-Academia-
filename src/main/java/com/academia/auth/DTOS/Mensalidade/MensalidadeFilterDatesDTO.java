package com.academia.auth.DTOS.Mensalidade;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public record MensalidadeFilterDatesDTO(

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataCriacaoInicio,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataCriacaoFim,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataPagamentoInicio,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataPagamentoFim,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataCancelamentoInicio,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataCancelamentoFim,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataVencimentoInicio,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataVencimentoFim
) 
{

}
