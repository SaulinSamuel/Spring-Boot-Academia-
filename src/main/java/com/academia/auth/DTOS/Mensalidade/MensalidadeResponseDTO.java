package com.academia.auth.DTOS.Mensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.academia.auth.Utils.StatusMensalidade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensalidadeResponseDTO {
    
    private Long id;

    private Integer diasTreino;

    private String aluno;

    private BigDecimal preco;

    private StatusMensalidade status;

    private LocalDate dataPagamento;

    private LocalDate dataCancelamento;

    private LocalDate dataVencimento;

    private LocalDate dataCriacao;
}
