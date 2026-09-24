package com.academia.auth.Listeners;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.MensalidadeCanceladaEvent;
import com.academia.auth.Events.MensalidadeCriadaEvent;
import com.academia.auth.Events.MensalidadePagaEvent;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.HistoricoMensalidadeMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.HistoricoMensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.HistoricoMensalidadeRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Services.NotificacaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MensalidadeListener {

    private final AcessoAcademiaRepository acessoAcademiaRepository;
    private final HistoricoMensalidadeRepository historicoMensalidadeRepository;
    private final MensalidadeRepository mensalidadeRepository;
    private final NotificacaoService notificacaoService;

    @EventListener
    public void aoCriarMensalidade(MensalidadeCriadaEvent event) {

        Usuario usuario = event.usuario();
        Optional<AcessoAcademia> acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario);
        LocalDate hoje = LocalDate.now();

        if (acessoAcademia.isEmpty()) {

            AcessoAcademia acessosAcademia = new AcessoAcademia();
            acessosAcademia.setUsuario(usuario);
            acessosAcademia.setInicioSemana(hoje.with(DayOfWeek.MONDAY));
            acessosAcademia.setDiasAcesso(0);
            acessosAcademia.setNome(usuario.getNome());
            usuario.setAcessosAcademia(acessosAcademia);
            
            acessoAcademiaRepository.save(acessosAcademia);
            
            log.info("Acesso da academia criado e salvo para usuário {}", usuario.getEmail());
        }
    }

    @EventListener
    public void aoMensalidadeSerPaga(MensalidadePagaEvent event) {

        var mensalidade = mensalidadeRepository.findById(event.mensalidadeId())
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        notificacaoService.enviarNotificacao(
            TipoNotificacao.MENSALIDADE_PAGA, 
            "Mensalidade paga!", 
            "Sua mensalidade foi paga com sucesso!", 
            List.of(mensalidade.getUsuario())
        );

        HistoricoMensalidade historicoMensalidade = 
            HistoricoMensalidadeMapper.toHistoricoMensalidade(mensalidade);

        historicoMensalidadeRepository.save(historicoMensalidade);
    }

    @EventListener
    public void aoMensalidadeSerCancelada(MensalidadeCanceladaEvent event) {

        var mensalidade = mensalidadeRepository.findById(event.mensalidadeId())
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        notificacaoService.enviarNotificacao(
            TipoNotificacao.MENSALIDADE_CANCELADA, 
            "Mensalidade cancelada!", 
            "Sua mensalidade foi cancelada!", 
            List.of(mensalidade.getUsuario())
        );

        HistoricoMensalidade historicoMensalidade = 
            HistoricoMensalidadeMapper.toHistoricoMensalidade(mensalidade);

        historicoMensalidadeRepository.save(historicoMensalidade);
    }

}
