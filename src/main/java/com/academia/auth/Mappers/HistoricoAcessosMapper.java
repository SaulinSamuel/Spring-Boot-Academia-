package com.academia.auth.Mappers;

import java.time.LocalDateTime;

import com.academia.auth.DTOS.HistoricoAcessos.HistoricoAcessosResponseDTO;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.HistoricoAcessos;
import com.academia.auth.Utils.TradutorDiaDaSemana;

public class HistoricoAcessosMapper {
    
    public static HistoricoAcessosResponseDTO toDTO(HistoricoAcessos historicoAcessos) {

        HistoricoAcessosResponseDTO dto = new HistoricoAcessosResponseDTO();
        TradutorDiaDaSemana tradutorDiaDaSemana = new TradutorDiaDaSemana();

        dto.setDiaDaSemana(tradutorDiaDaSemana.traduzirDiaSemana(
            LocalDateTime.now().getDayOfWeek())
        );
        dto.setId(historicoAcessos.getId());
        dto.setHorarioEntrada(historicoAcessos.getHorarioEntrada());
        dto.setRole(historicoAcessos.getRole());
        dto.setUsuario(historicoAcessos.getNomeUsuario());

        return dto;
    }

    public static HistoricoAcessos toHistoricoAcessosFromAcesso(AcessoAcademia acessoAcademia) {

        HistoricoAcessos historicoAcessos = new HistoricoAcessos();
        TradutorDiaDaSemana tradutorDiaDaSemana = new TradutorDiaDaSemana();

        historicoAcessos.setDiaDaSemana(tradutorDiaDaSemana.traduzirDiaSemana(
            LocalDateTime.now().getDayOfWeek())
        );
        historicoAcessos.setHorarioEntrada(LocalDateTime.now());
        historicoAcessos.setRole(acessoAcademia.getUsuario().getRole());
        historicoAcessos.setNomeUsuario(acessoAcademia.getNome());

        return historicoAcessos;
    }

}
