package com.academia.auth.Services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.academia.auth.DTOS.Notificacao.NotificacaoResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
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
                "Mensalidade atraso!",
                usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            notificacaoService.marcarNotificacaoComoLida(notificacao.getId());

            ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);

            verify(notificacaoRepository).save(captor.capture());

            var notificacaoCapturada = captor.getValue();

            assertThat(notificacaoCapturada.isLida()).isEqualTo(true);
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

    @Nested
    class MarcarTodasNotificacoesComoLidaTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = usuarioFactoryTest.criarUsuario();
        }

        @Test
        void deveMarcarTodasNotificacoesComoLida() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.marcarTodasNotificacoesComoLida(usuario.getId()))
                .thenReturn(2);

            notificacaoService.marcarTodasNotificacoesComoLida();

            verify(notificacaoRepository).marcarTodasNotificacoesComoLida(usuario.getId());
        
            verifyNoMoreInteractions(notificacaoRepository);
        }

    }

    @Nested
    class BuscarMinhasNotificacoesTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = usuarioFactoryTest.criarUsuario();
        }

        @Test
        void deveBuscarMinhasNotificacoes() {

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );

            var notificacao2 = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );
            notificacao2.setId(2L);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Notificacao> page = new PageImpl<>(List.of(notificacao, notificacao2));

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findAllByUsuario(usuario, pageable))
                .thenReturn(page);

            Page<NotificacaoResponseDTO> resultado = notificacaoService.buscarMinhasNotificacoes(pageable);

            assertThat(resultado).extracting(NotificacaoResponseDTO::id)
                .containsExactlyInAnyOrder(notificacao2.getId(), notificacao.getId());
        }

        @Test
        void deveRetornarVazioSeNaoTiverNotificacoes() {

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findAllByUsuario(usuario, pageable))
                .thenReturn(Page.empty());
        
            Page<NotificacaoResponseDTO> resultado = notificacaoService.buscarMinhasNotificacoes(pageable);

            assertThat(resultado).isEmpty();
        }
        
    }

    @Nested
    class BuscarNotificacaoPorIdTest {
    
        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = usuarioFactoryTest.criarUsuario();
        }

        @Test 
        void deveBuscarNotificacaoPorId() {

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            NotificacaoResponseDTO resultado = notificacaoService.buscarNotificacaoPorId(notificacao.getId());

            assertThat(resultado.id()).isEqualTo(notificacao.getId());
        }

        @Test
        void deveImpedirBuscarNotificacaoUsuarioNaoDono() {

            var usuarioNaoDono = usuarioFactoryTest.criarUsuario();
            usuarioNaoDono.setId(2L);
            usuarioNaoDono.setEmail("naodono@gmail.com");

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuarioNaoDono);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            assertThatThrownBy(() ->
                notificacaoService.buscarNotificacaoPorId(notificacao.getId())
            ).isInstanceOf(BusinessException.class)
            .hasMessage("Você não tem permissão de visualizar essa notificação!");

            verifyNoMoreInteractions(notificacaoRepository);
        }

    }

    @Nested
    class DeletarNotificacaoTest {

        private Usuario usuario;
        
        @BeforeEach
        void prepararSetup() {

            usuario = usuarioFactoryTest.criarUsuario();
        }

        @Test 
        void deveDeletarNotificacaoComSucesso() {

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            notificacaoService.deletarNotificacao(notificacao.getId());

            verify(notificacaoRepository).delete(notificacao);
            verifyNoMoreInteractions(notificacaoRepository);
        }

        @Test
        void deveImpedirDeletarNotificacaoUsuarioNaoDono() {

            var usuarioNaoDono = usuarioFactoryTest.criarUsuario();
            usuarioNaoDono.setId(2L);
            usuarioNaoDono.setEmail("naodono@gmail.com");

            var notificacao = criarNotificacao(
                TipoNotificacao.MENSALIDADE_ATRASADA, 
                "Mensalidade vai atrasar!",
                "Mensalidade atraso!",
                usuario
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuarioNaoDono);
            
            when(notificacaoRepository.findById(notificacao.getId()))
                .thenReturn(Optional.of(notificacao));

            assertThatThrownBy(() ->
                notificacaoService.deletarNotificacao(notificacao.getId())
            ).isInstanceOf(BusinessException.class)
            .hasMessage("Você não tem permissão de deletar essa notificação!");

            verifyNoMoreInteractions(notificacaoRepository);
        }

    }

}
