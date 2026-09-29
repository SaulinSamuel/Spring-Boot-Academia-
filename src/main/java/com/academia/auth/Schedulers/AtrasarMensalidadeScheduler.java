package com.academia.auth.Schedulers;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.MensalidadeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AtrasarMensalidadeScheduler {

    private final MensalidadeRepository mensalidadeRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void atrasarMensalidades() {

        LocalDate hoje = LocalDate.now();

        mensalidadeRepository.atrasarMensalidades(
            StatusMensalidade.ATRASADA, 
            StatusMensalidade.PENDENTE, 
            hoje
        );

    }
    
}
