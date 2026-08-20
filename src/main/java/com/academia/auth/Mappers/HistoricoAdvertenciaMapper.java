package com.academia.auth.Mappers;

import com.academia.auth.DTOS.HistoricoAdvertencia.HistoricoAdvertenciaResponseDTO;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.HistoricoAdvertencia;

public class HistoricoAdvertenciaMapper {
    
    public static HistoricoAdvertencia toEntityFromAdvertencia(Advertencia advertencia) {

        HistoricoAdvertencia historicoAdvertencia = new HistoricoAdvertencia();

        historicoAdvertencia.setDataCriacao(advertencia.getDataCriacao());
        historicoAdvertencia.setDataExpiracao(advertencia.getDataExpiracao());
        historicoAdvertencia.setMensagem(advertencia.getMensagem());
        historicoAdvertencia.setNivelAdvertencia(advertencia.getNivelAdvertencia());
        historicoAdvertencia.setDestinatario(advertencia.getDestinatario().getNome());
        historicoAdvertencia.setRemetente(advertencia.getRemetente().getNome());

        return historicoAdvertencia;
    }

    public static HistoricoAdvertenciaResponseDTO toDTO(HistoricoAdvertencia historicoAdvertencia) {

        HistoricoAdvertenciaResponseDTO dto = new HistoricoAdvertenciaResponseDTO();

        dto.setId(historicoAdvertencia.getId());
        dto.setDataCriacao(historicoAdvertencia.getDataCriacao());
        dto.setDestinatário(historicoAdvertencia.getDestinatario());
        dto.setDataExpiracao(historicoAdvertencia.getDataExpiracao());
        dto.setMensagem(historicoAdvertencia.getMensagem());
        dto.setRemetente(historicoAdvertencia.getRemetente());
        dto.setNivelAdvertencia(historicoAdvertencia.getNivelAdvertencia());
        
        return dto;
    }

}
