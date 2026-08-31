package com.academia.auth.Schedulers;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ConcluirAulasScheduler {
    
    private final AulaRepository aulaRepository;

    @Scheduled(fixedRate = 10 * 60 * 1000)
    @Transactional
    public void concluirAulas() {

        LocalDateTime agora = LocalDateTime.now();

        aulaRepository.concluirAulas(
            StatusAula.CONCLUIDA,
            StatusAula.CONFIRMADA,
            agora
        );

    }

}
