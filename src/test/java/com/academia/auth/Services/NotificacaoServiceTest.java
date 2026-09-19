package com.academia.auth.Services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.academia.auth.DTOS.Notificacao.NotificacaoResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.NotificacaoMapper;
import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.NotificacaoRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.factories.UsuarioFactoryTest;

@ExtendWith(MockitoExtension.class)
public class NotificacaoServiceTest {
    
    @Mock 
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock 
    private UsuarioAutenticadoService usuarioLogado;
    
    @Mock
    private NotificacaoMapper notificacaoMapper;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private UsuarioFactoryTest usuarioFactoryTest = new UsuarioFactoryTest();

    private Notificacao criarNotificacao(
        TipoNotificacao tipoNotificacao,
        String mensagem,
        String titulo,
        Usuario usuario
    )
    {
        
        Notificacao notificacao = new Notificacao();
        notificacao.setId(1L);
        notificacao.setLida(false);
        notificacao.setMensagem(mensagem);
        notificacao.setTipoNotificacao(tipoNotificacao);
        notificacao.setUsuario(usuario);

        return notificacao;
    }

    @Nested
    class EnviarNotificacaoTest {

        @Test 
        void deveEnviarNotificacaoComSucesso() {

            var usuario = usuarioFactoryTest.criarUsuario();

            var usuario2 = usuarioFactoryTest.criarUsuario();
            usuario2.setId(2L);
            usuario2.setEmail("teste2@gmail.com");

            List<Usuario> usuarios = List.of(usuario, usuario2);

            TipoNotificacao tipoNotificacao = TipoNotificacao.AULA_CONCLUIDA;
            String mensagem = "Aula concluida!";
            String titulo = "Aula concluída agora!";

            Notificacao notificacao = criarNotificacao(tipoNotificacao, mensagem, titulo, usuario);

            Notificacao notificacao2 = criarNotificacao(tipoNotificacao, mensagem, titulo, usuario2);
            notificacao2.setId(2L);

            when(notificacaoMapper.toEntity(usuario, tipoNotificacao, mensagem, titulo))
                .thenReturn(notificacao);

            when(notificacaoMapper.toEntity(usuario2, tipoNotificacao, mensagem, titulo))
                .thenReturn(notificacao2);

            notificacaoService.enviarNotificacao(
                tipoNotificacao, titulo, mensagem, usuarios
            );

            ArgumentCaptor<List<Notificacao>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificacaoRepository).saveAll(captor.capture());

            assertThat(captor.getValue())
                .containsExactly(notificacao, notificacao2);
        }

        @Test 
        void deveLancarExcecaoSeUsuariosVierVazio() {   

            TipoNotificacao tipoNotificacao = TipoNotificacao.AULA_CANCELADA;
            String mensagem = "Aula cancelada!";
            String titulo = "Aula cancelada!";
            List<Usuario> usuarios = List.of();

            assertThatThrownBy(() -> 
                notificacaoService.enviarNotificacao(
                    tipoNotificacao, titulo, mensagem, usuarios
                )
            ).isInstanceOf(ResourceNotFound.class)
            .hasMessage("Nenhum usuário encontrado!");

            verifyNoInteractions(notificacaoMapper, notificacaoRepository);
        }

    }

    @Nested 
    class MarcarNotificacaoComoLidaTest {

        Usuario usuario;

        @BeforeEach 
        void prepararSetup() {

            usuario = usuarioFactoryTest.criarUsuario();
        } 

        @Test 
        void deveMarcarNotificacaoComoLida() {

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!" ,
                usuario
            );

            NotificacaoResponseDTO dtoEsperado = new NotificacaoResponseDTO(
                1L,
                "Mensalidade atraso!",
                "Mensalidade vai atrasar!",
                TipoNotificacao.MENSALIDADE_ATRASADA,
                true
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            when(notificacaoMapper.toDTO(notificacao))
                .thenReturn(dtoEsperado);

            NotificacaoResponseDTO resultado = notificacaoService.marcarNotificacaoComoLida(notificacao.getId());

            assertThat(resultado.lida()).isEqualTo(true);

            verify(notificacaoRepository).save(notificacao);
        }

        @Test 
        void deveLancarExcecaoMarcarLidaUsuarioNaoDonoDaNotificacao() {

            var usuarioNaoDono = usuarioFactoryTest.criarUsuario();
            usuarioNaoDono.setId(2L);
            usuarioNaoDono.setEmail("naodono@gmail.com");

            var notificacao = criarNotificacao(
                TipoNotificacao.AULA_CONFIRMADA, "Aula confirmada", "Aula confirmada", usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuarioNaoDono);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            assertThatThrownBy(() ->
                notificacaoService.marcarNotificacaoComoLida(notificacao.getId())
            ).isInstanceOf(BusinessException.class)
            .hasMessage("Você não tem permissão de ler esta notificação!");

            verify(notificacaoRepository, never()).save(any(Notificacao.class));
        }

    } 

}
