package com.academia.auth.Schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.academia.auth.Services.MensalidadeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExcluirMensalidadeScheduler {
    
    private final MensalidadeService mensalidadeService;

    @Scheduled(cron = "0 0 0 * * *")
    public void excluirMensalidadesAposUmAno() {

        log.info("Começando exclusões de mensalidades após um ano");

        try {
            mensalidadeService.excluirMensalidadesAposAno();
            log.info("Exclusão de mensalidades após um ano concluídas");
        } catch (Exception e) {
            log.error("Erro ao tentar excluir mensalidades após um ano");
        }
       
    }

}
