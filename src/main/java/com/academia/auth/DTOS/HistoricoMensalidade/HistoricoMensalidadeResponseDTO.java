package com.academia.auth.DTOS.HistoricoMensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.academia.auth.Models.enums.StatusMensalidade;

public record HistoricoMensalidadeResponseDTO(
   
    String nomeUsuario,

    Integer diasTreino,

    BigDecimal valor,

    LocalDate dataCriacao,

    LocalDate dataPagamento,

    LocalDate dataVencimento,

    LocalDate dataCancelamento,

    StatusMensalidade status
) {
    
}
