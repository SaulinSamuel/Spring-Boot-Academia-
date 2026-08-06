package com.academia.auth.Models;

import java.time.LocalDateTime;

import com.academia.auth.Models.enums.AdvertenciaStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "historicoAdvertencias")
public class HistoricoAdvertencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AdvertenciaStatus nivelAdvertencia;

    @NotBlank
    private String remetente;

    @NotBlank
    private String destinatario;

    @NotBlank
    private String excluidoPor;

    @NotNull
    private LocalDateTime dataCriacao;

    @NotNull
    private LocalDateTime dataExpiracao;

    @NotNull
    private LocalDateTime dataExclusao;

}
