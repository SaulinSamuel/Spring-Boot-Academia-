package com.academia.auth.Schedulers;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;

import com.academia.auth.Repositories.AdvertenciaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExcluirAdvertenciaScheduler {
    
    private final AdvertenciaRepository advertenciaRepository;

    @Scheduled(cron = "0  0  0 * * *")
    public void excluirAdvertencias() {

        advertenciaRepository.excluirAdvertencias(LocalDateTime.now());
    }

}
