package com.academia.auth.DTOS.HistoricoAdvertencia;

import java.time.LocalDateTime;

import com.academia.auth.Models.enums.AdvertenciaStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoricoAdvertenciaResponseDTO {
    
    private Long id;

    private String mensagem;

    private AdvertenciaStatus nivelAdvertencia;

    private String remetente;

    private String destinatário;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataExpiracao;
}
