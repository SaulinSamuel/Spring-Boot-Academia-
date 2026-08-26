package com.academia.auth.Listeners;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.academia.auth.Events.UsuarioPromovidoEvent;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Repositories.AcessoAcademiaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UsuarioListener {

    private final AcessoAcademiaRepository acessoAcademiaRepository;

    @EventListener
    public void aoPromoverUsuario(UsuarioPromovidoEvent event) {

        Usuario usuarioPromovido = event.usuario();
        Optional<AcessoAcademia> acessoAcademiaExistente = acessoAcademiaRepository.findByUsuario(usuarioPromovido);
        
        if (acessoAcademiaExistente.isEmpty()) {

            AcessoAcademia acessoAcademia = new AcessoAcademia();

            acessoAcademia.setUsuario(usuarioPromovido);
            acessoAcademia.setInicioSemana(LocalDate.now().with(DayOfWeek.MONDAY));
            acessoAcademia.setDiasAcesso(0);
            acessoAcademia.setNome(usuarioPromovido.getNome());
            usuarioPromovido.setAcessosAcademia(acessoAcademia);

            acessoAcademiaRepository.save(acessoAcademia);

            log.info("Acesso academia criado para usuário {}", usuarioPromovido.getEmail());
        }

    }

}
