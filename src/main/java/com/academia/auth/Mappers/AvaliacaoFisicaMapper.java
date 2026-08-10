package com.academia.auth.Mappers;

import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoRequestDTO;
import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoResponseDTO;
import com.academia.auth.Models.AvaliacaoFisica;

public class AvaliacaoFisicaMapper {
    
    public static AvaliacaoFisica toEntity(AvaliacaoRequestDTO dto) {

        AvaliacaoFisica avaliacaoFisica = new AvaliacaoFisica();

        avaliacaoFisica.setAltura(dto.getAltura());
        avaliacaoFisica.setBraco(dto.getBraco());
        avaliacaoFisica.setCintura(dto.getCintura());
        avaliacaoFisica.setIdade(dto.getIdade());
        avaliacaoFisica.setMassaMuscular(dto.getMassaMuscular());
        avaliacaoFisica.setPeito(dto.getPeito());
        avaliacaoFisica.setPercentualGordura(dto.getPercentualGordura());
        avaliacaoFisica.setPeso(dto.getPeso());

        return avaliacaoFisica;
    }

    public static AvaliacaoResponseDTO toDTO(AvaliacaoFisica avaliacaoFisica) {

        AvaliacaoResponseDTO dto = new AvaliacaoResponseDTO();

        dto.setAltura(avaliacaoFisica.getAltura());
        dto.setBraco(avaliacaoFisica.getBraco());
        dto.setCintura(avaliacaoFisica.getCintura());
        dto.setDataAvaliacao(avaliacaoFisica.getDataAvaliacao());
        dto.setId(avaliacaoFisica.getId());
        dto.setIdade(avaliacaoFisica.getIdade());
        dto.setMassaMuscular(avaliacaoFisica.getMassaMuscular());
        dto.setNome(avaliacaoFisica.getAluno().getNome());
        dto.setAvaliador(avaliacaoFisica.getAvaliador().getNome());
        dto.setPeito(avaliacaoFisica.getPeito());
        dto.setPercentualGordura(avaliacaoFisica.getPercentualGordura());

        return dto;
    }

}
