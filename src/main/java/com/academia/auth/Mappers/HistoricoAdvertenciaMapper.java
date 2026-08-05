package com.academia.auth.Mappers;

import java.time.LocalDateTime;

import com.academia.auth.DTOS.HistoricoAdvertencia.HistoricoAdvertenciaResponseDTO;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.HistoricoAdvertencia;
import com.academia.auth.Models.Usuario;

public class HistoricoAdvertenciaMapper {
    
    public static HistoricoAdvertencia toEntityFromAdvertencia(Advertencia advertencia, Usuario usuario) {

        HistoricoAdvertencia historicoAdvertencia = new HistoricoAdvertencia();

        historicoAdvertencia.setDataCriacao(advertencia.getDataCriacao());
        historicoAdvertencia.setDataExclusao(LocalDateTime.now());
        historicoAdvertencia.setDataExpiracao(advertencia.getDataExpiracao());
        historicoAdvertencia.setMensagem(advertencia.getMensagem());
        historicoAdvertencia.setNivelAdvertencia(advertencia.getNivelAdvertencia());
        historicoAdvertencia.setDestinatario(advertencia.getDestinatario().getNome());
        historicoAdvertencia.setExcluidoPor(usuario.getNome());
        historicoAdvertencia.setRemetente(advertencia.getRemetente().getNome());

        return historicoAdvertencia;
    }

    public static HistoricoAdvertenciaResponseDTO toDTO(HistoricoAdvertencia historicoAdvertencia) {

        HistoricoAdvertenciaResponseDTO dto = new HistoricoAdvertenciaResponseDTO();

        dto.setId(historicoAdvertencia.getId());
        dto.setDataCriacao(historicoAdvertencia.getDataCriacao());
        dto.setDataExclusão(LocalDateTime.now());
        dto.setDestinatário(historicoAdvertencia.getDestinatario());
        dto.setExcluidoPor(historicoAdvertencia.getExcluidoPor());
        dto.setDataExpiracao(historicoAdvertencia.getDataExpiracao());
        dto.setMensagem(historicoAdvertencia.getMensagem());
        dto.setRemetente(historicoAdvertencia.getRemetente());
        dto.setNivelAdvertencia(historicoAdvertencia.getNivelAdvertencia());
        
        return dto;
    }

}
