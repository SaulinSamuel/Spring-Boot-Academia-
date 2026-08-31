package com.academia.auth.Models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.academia.auth.Models.enums.StatusAula;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
@Table(name = "aulas")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private LocalDate dataAula;
    
    @Column(nullable = false)
    private LocalTime horarioInicio;
    
    @Column(nullable = false)
    private LocalTime horarioFim;
    
    @Column(nullable = false)
    private Integer capacidadeInscricoes;
    
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAula status = StatusAula.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Usuario instrutor;

    @OneToMany(
        mappedBy = "aula", 
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true    
    )
    @Builder.Default
    List<Agendamento> agendamentos = new ArrayList<>();

}
