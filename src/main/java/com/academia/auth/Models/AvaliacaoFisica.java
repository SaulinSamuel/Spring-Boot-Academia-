package com.academia.auth.Models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "avaliacoes_fisicas")
public class AvaliacaoFisica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private Double altura;

    @Column(nullable = false)
    private Integer idade;

    @Column(name = "percentual_gordura", nullable = false)
    private Double percentualGordura;

    @Column(name = "massa_muscular", nullable = false)
    private Double massaMuscular;

    @Column(nullable = false)
    private Double braco;

    @Column(nullable = false)
    private Double peito;

    @Column(nullable = false)
    private Double cintura;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliador_id", nullable = false)
    private Usuario avaliador;

}
