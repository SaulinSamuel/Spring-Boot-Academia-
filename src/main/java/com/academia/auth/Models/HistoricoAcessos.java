package com.academia.auth.Models;

import java.time.LocalDateTime;

import com.academia.auth.Models.enums.DiasSemana;
import com.academia.auth.Models.enums.RoleUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "historico_acessos")
public class HistoricoAcessos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeUsuario;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleUser role;

    @Column(nullable = false)
    private LocalDateTime horarioEntrada;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiasSemana diaDaSemana;

}
