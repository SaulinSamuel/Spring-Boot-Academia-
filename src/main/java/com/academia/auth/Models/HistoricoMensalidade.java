package com.academia.auth.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.academia.auth.Models.enums.StatusMensalidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "historico_mensalidades")
public class HistoricoMensalidade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeUsuario;

    @Column(nullable = false)
    private Integer diasTreino;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataCriacao;

    private LocalDate dataPagamento;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataCancelamento;

    @Column(nullable = false)
    private StatusMensalidade status;

}
