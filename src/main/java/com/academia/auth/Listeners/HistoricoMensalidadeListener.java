package com.academia.auth.Listeners;

import com.academia.auth.Repositories.HistoricoMensalidadeRepository;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.MensalidadeStatusAlteradoEvent;
import com.academia.auth.Mappers.HistoricoMensalidadeMapper;
import com.academia.auth.Models.HistoricoMensalidade;
import com.academia.auth.Models.Mensalidade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class HistoricoMensalidadeListener {

    private final HistoricoMensalidadeRepository historicoMensalidadeRepository;
    
    @EventListener
    public void aoAlterarStatus(MensalidadeStatusAlteradoEvent event) {

        log.info(">>> LISTENER DISPAROU para mensalidade: {}", event.mensalidade().getId());

            Mensalidade mensalidade = event.mensalidade();

            HistoricoMensalidade historicoMensalidade = 
                HistoricoMensalidadeMapper.toHistoricoMensalidade(mensalidade);

            historicoMensalidadeRepository.save(historicoMensalidade);
    } 

}
