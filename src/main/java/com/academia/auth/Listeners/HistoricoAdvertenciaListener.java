package com.academia.auth.Listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.AdvertenciaCriadaEvent;
import com.academia.auth.Mappers.HistoricoAdvertenciaMapper;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.HistoricoAdvertencia;
import com.academia.auth.Repositories.HistoricoAdvertenciaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HistoricoAdvertenciaListener {

    private final HistoricoAdvertenciaRepository historicoAdvertenciaRepository;

    @EventListener
    public void aoCriarMensalidade(AdvertenciaCriadaEvent event) {

        Advertencia advertencia = event.advertencia();

        HistoricoAdvertencia historicoAdvertencia = 
            HistoricoAdvertenciaMapper.toEntityFromAdvertencia(advertencia);

        historicoAdvertenciaRepository.save(historicoAdvertencia);
    }

}
