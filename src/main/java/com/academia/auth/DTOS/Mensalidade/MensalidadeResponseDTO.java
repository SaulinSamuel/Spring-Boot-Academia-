package com.academia.auth.DTOS.Mensalidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private LocalDateTime dataPagamento;

    private LocalDateTime dataVencimento;

    private LocalDateTime dataCriacao;
}
