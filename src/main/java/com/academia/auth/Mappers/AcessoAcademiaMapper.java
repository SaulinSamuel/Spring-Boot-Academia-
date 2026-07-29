package com.academia.auth.Mappers;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaResponseDTO;
import com.academia.auth.Models.AcessoAcademia;

public class AcessoAcademiaMapper {
    
    public static AcessoAcademiaResponseDTO toDTO(AcessoAcademia acesso) {
       
        AcessoAcademiaResponseDTO dto = new AcessoAcademiaResponseDTO();

        dto.setDiasAcesso(acesso.getDiasAcesso());
        dto.setInicioSemana(acesso.getInicioSemana());
        dto.setUltimoAcesso(acesso.getUltimoAcesso());
        dto.setUsuario(acesso.getUsuario().getNome());

        return dto;
    }

}
