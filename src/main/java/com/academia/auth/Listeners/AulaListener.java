package com.academia.auth.Listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.AulaCanceladaEvent;
import com.academia.auth.Events.AulaConfirmadaEvent;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.AgendamentoRepository;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.NotificacaoService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class AulaListener {
    
    private final NotificacaoService notificacaoService;
    private final AgendamentoRepository agendamentoRepository;
    private final AulaRepository aulaRepository;

    @EventListener 
    public void aoAulaSerConfirmada(AulaConfirmadaEvent event) {

        var usuarios = agendamentoRepository.findUsuariosByAulaIdAndStatus(event.aulaId());

        var aula = aulaRepository.findById(event.aulaId());

        notificacaoService.enviarNotificacao(
            TipoNotificacao.AULA_CONFIRMADA, 
            "Aula " + aula.get().getNome() + " confirmada!",
            "Sua aula do dia " + aula.get().getDataAula() + " foi confirmada!", 
            usuarios
        );
    }

    @EventListener
    public void aoAulaSerCancelada(AulaCanceladaEvent event) {

        var usuarios = agendamentoRepository.findUsuariosByAulaIdAndStatus(event.aulaId());

        var aula = aulaRepository.findById(event.aulaId());

        notificacaoService.enviarNotificacao(
            TipoNotificacao.AULA_CANCELADA, 
            "Aula " + aula.get().getNome() + " cancelada!", 
            "Sua aula do dia " + aula.get().getDataAula() + " foi cancelada!", 
            usuarios
        );
    }

}
