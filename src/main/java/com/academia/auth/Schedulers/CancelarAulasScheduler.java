package com.academia.auth.Schedulers;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CancelarAulasScheduler {
    
    private final AulaRepository aulaRepository;

    @Scheduled(fixedRate = 15 * 60 * 1000) // roda a cada 15 minutos
    @Transactional
    public void cancelarAulas() {

        LocalDateTime agora = LocalDateTime.now();

        LocalDateTime tempoLimite = agora.plusHours(2);

        aulaRepository.cancelarAulas(
            StatusAula.PENDENTE, 
            StatusAula.CANCELADA,
            tempoLimite
        );
    } 

}
