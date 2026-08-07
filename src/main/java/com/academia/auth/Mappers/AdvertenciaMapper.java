package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Advertencia.AdvertenciaRequestDTO;
import com.academia.auth.DTOS.Advertencia.AdvertenciaResponseDTO;
import com.academia.auth.Models.Advertencia;

public class AdvertenciaMapper {
    
    public static Advertencia toEntity(AdvertenciaRequestDTO dto) {

        Advertencia advertencia = new Advertencia();

        advertencia.setMensagem(dto.getMensagem());
        advertencia.setNivelAdvertencia(dto.getNivel());

        return advertencia;
    }

    public static AdvertenciaResponseDTO toDTO(Advertencia advertencia) {

        AdvertenciaResponseDTO dto = new AdvertenciaResponseDTO();

        dto.setId(advertencia.getId());
        dto.setMensagem(advertencia.getMensagem());
        dto.setRemetente(advertencia.getRemetente().getNome());
        dto.setNivelAdvertencia(advertencia.getNivelAdvertencia());
        dto.setDestinatario(advertencia.getDestinatario().getNome());
        dto.setDataCriacao(advertencia.getDataCriacao());
        dto.setDataExpiracao(advertencia.getDataExpiracao());
        
        return dto;
    }

}
