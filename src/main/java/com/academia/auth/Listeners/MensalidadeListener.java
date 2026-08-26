package com.academia.auth.Listeners;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.MensalidadeCriadaEvent;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Repositories.AcessoAcademiaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MensalidadeListener {

    private final AcessoAcademiaRepository acessoAcademiaRepository;

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
        }
    }

}
