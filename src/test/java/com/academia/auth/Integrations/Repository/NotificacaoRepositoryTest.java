package com.academia.auth.Integrations.Repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.NotificacaoRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;
import com.academia.auth.factories.UsuarioFactoryTest;

@DataJpaTest
@Import(TestContainersConfig.class)
public class NotificacaoRepositoryTest {
    
    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioFactoryTest usuarioFactoryTest = new UsuarioFactoryTest();
    
    private Notificacao criarNotificacao(
        TipoNotificacao tipoNotificacao,
        String mensagem,
        String titulo,
        Usuario usuario
    )
    {
        
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(false);
        notificacao.setMensagem(mensagem);
        notificacao.setTitulo(titulo);
        notificacao.setTipoNotificacao(tipoNotificacao);
        notificacao.setUsuario(usuario);

        return notificacao;
    }

    @Test
    void deveBuscarTodasNotificacoesPorUsuario() {

        var usuario = usuarioFactoryTest.criarUsuarioSemId();
        usuarioRepository.save(usuario);

        var notificacao = criarNotificacao(
            TipoNotificacao.AULA_CANCELADA,
            "Aula cancelada!", 
            "Aula cancelada!", 
            usuario
        );

        var notificacao2 = criarNotificacao(
            TipoNotificacao.AULA_CANCELADA,
            "Aula cancelada!", 
            "Aula cancelada!", 
            usuario
        );

        Pageable pageable = PageRequest.of(0, 10);

        notificacaoRepository.saveAll(List.of(notificacao, notificacao2));

        Page<Notificacao> notificacoes = notificacaoRepository.findAllByUsuario(usuario, pageable);
    
        assertThat(notificacoes).extracting(Notificacao::getId)
            .containsExactlyInAnyOrder(notificacao.getId(), notificacao2.getId());
    }

    @Test
    void deveMarcarTodasNotificacoesComoLida() {

        var usuario = usuarioFactoryTest.criarUsuarioSemId();
        usuarioRepository.save(usuario);

        var notificacao = criarNotificacao(
            TipoNotificacao.AULA_CANCELADA,
            "Aula cancelada!", 
            "Aula cancelada!", 
            usuario
        );

        var notificacao2 = criarNotificacao(
            TipoNotificacao.AULA_CANCELADA,
            "Aula cancelada!", 
            "Aula cancelada!", 
            usuario
        );

        notificacaoRepository.saveAll(List.of(notificacao, notificacao2));

        int notificacoesAtualizadas = notificacaoRepository.marcarTodasNotificacoesComoLida(usuario.getId());

        List<Notificacao> notificacoes = notificacaoRepository.findAll();

        assertThat(notificacoesAtualizadas).isEqualTo(2);
        assertThat(notificacoes).extracting(Notificacao::isLida)
            .containsOnly(true);
    }

}
