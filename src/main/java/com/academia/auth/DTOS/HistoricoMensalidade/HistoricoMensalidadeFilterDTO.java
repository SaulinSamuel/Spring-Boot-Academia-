package com.academia.auth.DTOS.HistoricoMensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricoMensalidadeFilterDTO(
    String nomeUsuario,
    Integer diasTreino,
    BigDecimal valor,
    LocalDate dataPagamentoInicio,
    LocalDate dataPagamentoFim,
    LocalDate dataCancelamentoInicio,
    LocalDate dataCancelamentoFim
) {
    
}
