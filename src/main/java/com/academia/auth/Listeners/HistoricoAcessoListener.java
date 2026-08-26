package com.academia.auth.Listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.AcessarAcademiaEvent;
import com.academia.auth.Mappers.HistoricoAcessosMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.HistoricoAcessos;
import com.academia.auth.Repositories.HistoricoAcessosRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HistoricoAcessoListener {

    private final HistoricoAcessosRepository historicoAcessosRepository;

    @EventListener
    public void aoAcessarAcademia(AcessarAcademiaEvent event) {

        AcessoAcademia acessoAcademia = event.acessoAcademia();

        HistoricoAcessos historicoAcessos = 
            HistoricoAcessosMapper.toHistoricoAcessosFromAcesso(acessoAcademia);

        historicoAcessosRepository.save(historicoAcessos);
    }   

}
