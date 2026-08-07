package com.academia.auth.DTOS.Advertencia;

import java.time.LocalDateTime;

import com.academia.auth.Models.enums.AdvertenciaStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdvertenciaResponseDTO {
    
    private Long id;

    private String mensagem;

    private AdvertenciaStatus nivelAdvertencia;

    private String remetente;

    private String destinatario;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataExpiracao;

}
