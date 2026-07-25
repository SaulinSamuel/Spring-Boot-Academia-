package com.academia.auth.Schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.academia.auth.Services.MensalidadeService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AtrasarMensalidadeScheduler {

    private final MensalidadeService mensalidadeService;

    @Scheduled(cron = "0 0 0 * * *")
    public void atrasarMensalidades() {

        mensalidadeService.atrasarMensalidades();

    }
    
}
