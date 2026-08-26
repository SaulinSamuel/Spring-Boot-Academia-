package com.academia.auth.Schedulers;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.academia.auth.Repositories.MensalidadeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExcluirMensalidadeScheduler {
    
    private final MensalidadeRepository mensalidadeRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void excluirMensalidadesAposUmAno() {

        log.info("Começando exclusões de mensalidades após um ano");
        LocalDate hoje = LocalDate.now();
        LocalDate umAno = hoje.minusYears(1);

        try {
            mensalidadeRepository.excluirMensalidadesAposAno(umAno);
            log.info("Exclusão de mensalidades após um ano concluídas");
        } catch (Exception e) {
            log.error("Erro ao tentar excluir mensalidades após um ano...");
            log.error(e.getMessage());
        }
       
    }

}
