package com.academia.auth.Schedulers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Services.NotificacaoService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class NotificarMensalidadeScheduler {
    
    private final NotificacaoService notificacaoService;
    private final MensalidadeRepository mensalidadeRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void NotificarMensalidadeAtrasada() {

        LocalDate dataVencimento = LocalDate.now().plusDays(2);

        List<Usuario> usuarios = mensalidadeRepository.findUsuariosPorDataVencimento(
            dataVencimento
        );

        notificacaoService.enviarNotificacao(
            TipoNotificacao.MENSALIDADE_ATRASADA,
            "Sua mensalidade irá vencer em dois dias!", 
            "Atente-se na renovação da sua mensalidade!", 
            usuarios
        );
    }

}
