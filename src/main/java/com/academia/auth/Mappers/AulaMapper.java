package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Aula.AulaRequestDTO;
import com.academia.auth.DTOS.Aula.AulaResponseDTO;
import com.academia.auth.Models.Aula;

public class AulaMapper {
    
    public static Aula toEntity(AulaRequestDTO dto) {

        Aula aula = Aula.builder()
            .capacidadeInscricoes(dto.capacidadeInscricoes())
            .dataAula(dto.dataAula())
            .horarioFim(dto.horarioFim())
            .horarioInicio(dto.horarioInicio())
        .build();

        return aula;
    }

    public static AulaResponseDTO toDTO(Aula aula) {

        AulaResponseDTO dto = AulaResponseDTO.builder()
            .capacidadeInscricoes(aula.getCapacidadeInscricoes())
            .dataAula(aula.getDataAula())
            .horarioFim(aula.getHorarioFim())
            .horarioInicio(aula.getHorarioInicio())
            .id(aula.getId())
            .instrutor(aula.getInstrutor().getNome())
            .nome(aula.getNome())
            .status(aula.getStatus())
        .build();

        return dto;
    }

}
