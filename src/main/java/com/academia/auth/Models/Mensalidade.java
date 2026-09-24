package com.academia.auth.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.enums.StatusMensalidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "mensalidade")
public class Mensalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer diasTreino;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataCriacao;

    private LocalDate dataPagamento;

    private LocalDate dataCancelamento;

    @Builder.Default
    private Integer atualizacoes = 0;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusMensalidade status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public void criar(BigDecimal valor, LocalDate hoje, Integer diasTreino, Usuario usuario) {

        this.setValor(valor);
        this.setDataCriacao(hoje);
        this.setDataVencimento(hoje.plusMonths(1));
        this.setUsuario(usuario);
        this.setDiasTreino(diasTreino);
        this.setDataPagamento(null);
        this.setDataCancelamento(null);
        this.setAtualizacoes(0);
        this.setStatus(StatusMensalidade.PENDENTE);
    }

    public void atualizar(Integer diasTreino, BigDecimal valor) {

        if (status != StatusMensalidade.PENDENTE) {
            throw new BusinessException("Apenas mensalidades pendentes podem ser alteradas!");
        }

        if (atualizacoes >= 1) {
            throw new BusinessException("Você só pode atualizar sua mensalidade 1 vez por mês!");
        }

        this.setDiasTreino(diasTreino);
        this.setValor(valor);
        this.setAtualizacoes(1);    
    }

    public void pagar() {

        if (status != StatusMensalidade.PENDENTE && 
            status != StatusMensalidade.ATRASADA) {
            
            throw new BusinessException("Apenas mensalidades pendentes(ou atrasadas) podem ser pagas!");
        }

        this.setStatus(StatusMensalidade.PAGA);
        this.setDataPagamento(LocalDate.now());
    }

    public void cancelar() {

        if (status != StatusMensalidade.PENDENTE) {
            
            throw new BusinessException("Apenas mensalidades pendentes podem ser canceladas!");
        }

        this.setStatus(StatusMensalidade.CANCELADA);
        this.setDataCancelamento(LocalDate.now());
    }

}
