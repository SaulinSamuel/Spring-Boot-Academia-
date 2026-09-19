package com.academia.auth.Listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.AulaCanceladaEvent;
import com.academia.auth.Events.AulaConfirmadaEvent;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.AgendamentoRepository;
import com.academia.auth.Services.NotificacaoService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class AulaListener {
    
    private final NotificacaoService notificacaoService;
    private final AgendamentoRepository agendamentoRepository;

    @EventListener 
    public void aoAulaSerConfirmada(AulaConfirmadaEvent event) {

        var usuarios = agendamentoRepository.findUsuariosByAulaIdAndStatus(event.aulaId());

        notificacaoService.enviarNotificacao(
            TipoNotificacao.AULA_CONFIRMADA, 
            "Aula " + event.nomeAula() + " confirmada!",
            "Sua aula do dia " + event.dataAula() + " foi confirmada!", 
            usuarios
        );
    }

    @EventListener
    public void aoAulaSerCancelada(AulaCanceladaEvent event) {

        var usuarios = agendamentoRepository.findUsuariosByAulaIdAndStatus(event.aulaId());

        notificacaoService.enviarNotificacao(
            TipoNotificacao.AULA_CANCELADA, 
            "Aula " + event.nomeAula() + " cancelada!", 
            "Sua aula do dia " + event.dataAula() + " foi cancelada!", 
            usuarios
        );
    }

}
