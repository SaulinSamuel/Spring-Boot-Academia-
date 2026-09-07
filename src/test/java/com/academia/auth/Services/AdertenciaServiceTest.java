package com.academia.auth.Services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.Advertencia.AdvertenciaRequestDTO;
import com.academia.auth.DTOS.Advertencia.AdvertenciaResponseDTO;
import com.academia.auth.Events.AdvertenciaCriadaEvent;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AdvertenciaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.factories.UsuarioFactoryTest;

@ExtendWith(MockitoExtension.class)
public class AdertenciaServiceTest {
    
    @Mock 
    private AdvertenciaRepository advertenciaRepository;

    @Mock 
    private UsuarioRepository usuarioRepository;

    @Mock 
    private UsuarioAutenticadoService usuarioLogado;

    @Mock 
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks 
    private AdvertenciaService advertenciaService;

    private UsuarioFactoryTest usuarioFactoryTest = new UsuarioFactoryTest();

    private Usuario usuario;

    private AdvertenciaRequestDTO criarAdvertenciaRequest() {
        return new AdvertenciaRequestDTO(
            "Equipamento quebradio!",
            AdvertenciaStatus.LEVE
        );
    }

    private Advertencia criarAdvertencia(Usuario remetente, Usuario destinatario) {
        
        LocalDateTime agora = LocalDateTime.now();

        return Advertencia.builder()
            .id(1L)
            .mensagem("Equipamento quebrado!")
            .nivelAdvertencia(AdvertenciaStatus.LEVE)
            .dataCriacao(agora)
            .dataExpiracao(agora.plusDays(3))
            .destinatario(destinatario)
            .remetente(remetente)
        .build();
    }

    @BeforeEach
    void usuarioLogado() {
        usuario = usuarioFactoryTest.criarUsuario();
    }

    @Nested 
    class EnviarAdvertenciaTest {

        @Test 
        void deveEnviarAdertenciaComSucesso() {

            var request = criarAdvertenciaRequest();

            usuario.setRole(RoleUser.ROLE_INSTRUTOR);

            var usuarioAdvertido = usuarioFactoryTest.criarUsuario();
            usuarioAdvertido.setId(2L);
            usuarioAdvertido.setEmail("aluno@gmail.com");

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(usuarioAdvertido.getId()))
                .thenReturn(Optional.of(usuarioAdvertido));

            when(advertenciaRepository.countByDestinatario(usuarioAdvertido))
                .thenReturn(0L);

            AdvertenciaResponseDTO resultado = advertenciaService.enviarAdvertencia(
                request, 
                usuarioAdvertido.getId()
            );

            assertThat(resultado.getNivelAdvertencia()).isEqualTo(request.getNivel());
            assertThat(resultado.getDestinatario()).isEqualTo(usuarioAdvertido.getNome());
            assertThat(resultado.getRemetente()).isEqualTo(usuario.getNome());
            assertThat(resultado.getMensagem()).isEqualTo(request.getMensagem());

            verify(advertenciaRepository).save(any(Advertencia.class));
            verify(applicationEventPublisher).publishEvent(any(AdvertenciaCriadaEvent.class));
        }

        @Test 
        void deveImpedirEnviarAdertenciaUsuarioSemPermissao() {

            var request = criarAdvertenciaRequest();

            var usuarioAdvertido = usuarioFactoryTest.criarUsuario();
            usuarioAdvertido.setId(2L);
            usuarioAdvertido.setEmail("aluno@gmail.com");

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(usuarioAdvertido.getId()))
                .thenReturn(Optional.of(usuarioAdvertido));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.enviarAdvertencia(request, usuarioAdvertido.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode enviar advertências para funcionários!");

            verify(advertenciaRepository, never()).save(any(Advertencia.class));
        }

        @Test
        void deveImpedirEnviarAdvertenciaUsuarioComTresOuMaisAdvertencias() {

            var request = criarAdvertenciaRequest();

            var usuarioAdvertido = usuarioFactoryTest.criarUsuario();
            usuarioAdvertido.setId(2L);
            usuarioAdvertido.setEmail("aluno@gmail.com");

            usuario.setRole(RoleUser.ROLE_INSTRUTOR);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(usuarioAdvertido.getId()))
                .thenReturn(Optional.of(usuarioAdvertido));

            when(advertenciaRepository.countByDestinatario(usuarioAdvertido))
                .thenReturn(3L);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.enviarAdvertencia(request, usuarioAdvertido.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Este usuário já possui 3 advertências!");

            verify(advertenciaRepository, never()).save(any(Advertencia.class));
            verify(applicationEventPublisher, never()).publishEvent(anyIterable());
        }

    }

    @Nested 
    class MostrarSuasAdvertenciasRecebidasTest {

        @Test 
        void deveMostrarSuasAdvertenciasRecebidas() {

            var destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(2L);
            destinatario.setEmail("destinatario@gmail.com");

            Pageable pageable = PageRequest.of(0, 10);

            var advertencia = criarAdvertencia(usuario, destinatario);

            var advertencia2 = criarAdvertencia(usuario, destinatario);
            advertencia.setId(2L);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            Page<Advertencia> page = new PageImpl<>(advertencias);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findAllByDestinatario(usuario, pageable))
                .thenReturn(page);

            Page<AdvertenciaResponseDTO> resultado = advertenciaService.mostrarSuasAdvertenciasRecebidas(pageable);

            assertThat(resultado.getContent()).extracting(AdvertenciaResponseDTO::getId)
                .containsExactlyInAnyOrder(advertencia2.getId(), advertencia.getId());
        }
    
    }

    @Nested     
    class MostrarSuasAdvertenciasEnviadasTest {

        @Test 
        void deveMostrarSuasAdvertenciasEnviadas() {

            var destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(2L);
            destinatario.setEmail("destinatario@gmail.com");

            usuario.setRole(RoleUser.ROLE_INSTRUTOR);

            Pageable pageable = PageRequest.of(0, 10);

            var advertencia = criarAdvertencia(usuario, destinatario);

            var advertencia2 = criarAdvertencia(usuario, destinatario);
            advertencia.setId(2L);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            Page<Advertencia> page = new PageImpl<>(advertencias);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findAllByRemetente(usuario, pageable))
                .thenReturn(page);

            Page<AdvertenciaResponseDTO> resultado = advertenciaService.mostrarSuasAdvertenciasEnviadas(pageable);

            assertThat(resultado.getContent()).extracting(AdvertenciaResponseDTO::getId)
                .containsExactlyInAnyOrder(advertencia2.getId(), advertencia.getId());
        }

        @Test 
        void deveImpedirMostrarUsuarioSemPermissao() {

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.mostrarSuasAdvertenciasEnviadas(pageable)            
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar essas advertências!");
        }

    }

    @Nested 
    class BuscarTodasAdvertenciasPorFiltroTest {

        Usuario destinatario;
        Advertencia advertencia;
        Advertencia advertencia2;
        List<Advertencia> advertencias;

        @BeforeEach 
        void prepararSetup() {

            destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(2L);
            destinatario.setEmail("destinatario@gmail.com");

            advertencia = criarAdvertencia(usuario, destinatario);

            advertencia2 = criarAdvertencia(usuario, destinatario);
            advertencia.setId(2L);

            advertencias = List.of(advertencia, advertencia2);
        }

        @Test 
        void deveBuscarTodasAdvertencias() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Advertencia> page = new PageImpl<>(advertencias);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

            Page<AdvertenciaResponseDTO> resultado = advertenciaService.buscarTodasAdvertenciasPorFiltro(
                null, 
                null, 
                null, 
                null, 
                null, 
                pageable
            );

            assertThat(resultado.getContent()).extracting(AdvertenciaResponseDTO::getId)
                .containsExactlyInAnyOrder(advertencia2.getId(), advertencia.getId());
        }

        @Test 
        void deveBuscarTodasAdvertenciasComFiltros() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            String remetente = "test";

            AdvertenciaStatus status = AdvertenciaStatus.MODERADA;

            advertencia.setNivelAdvertencia(AdvertenciaStatus.MODERADA);

            Page<Advertencia> page = new PageImpl<>(advertencias);

            Pageable pageable = PageRequest.of(0, 10);

            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime umDiaAtras = agora.minusDays(1);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

            Page<AdvertenciaResponseDTO> resultado = advertenciaService.buscarTodasAdvertenciasPorFiltro(
                remetente, 
                null, 
                status, 
                umDiaAtras, 
                null, 
                pageable
            );

            assertThat(resultado.getContent()).extracting(AdvertenciaResponseDTO::getId)
                .contains(advertencia.getId());
        }

        @Test 
        void deveImpedirBuscarAdvertenciasSemPermissao() {

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.buscarTodasAdvertenciasPorFiltro(
                    null, 
                    null, 
                    null, 
                    null, 
                    null, 
                    pageable
                )
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de visualizar as advertências!");
        }

    }

    @Nested 
    class BuscarAdvertenciaPorIdTest {

        @Test 
        void deveBuscarAdvertenciaPorIdComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            var destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(2L);
            destinatario.setEmail("destinatario@gmail.com");

            var advertencia = criarAdvertencia(usuario, destinatario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findById(advertencia.getId()))
                .thenReturn(Optional.of(advertencia));

            AdvertenciaResponseDTO resultado = advertenciaService.buscarAdvertenciaPorId(advertencia.getId());

            assertThat(resultado.getId()).isEqualTo(advertencia.getId());
            assertThat(resultado.getDestinatario()).isEqualTo(destinatario.getNome());
        }

        @Test 
        void deveImpedirBuscarAdvertenciaUsuarioSemPermissao() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.buscarAdvertenciaPorId(1L)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de visualizar essa advertência!");
        }

    }

    @Nested 
    class ExcluirAdvertenciaTest {

        @Test 
        void deveExcluirAdvertenciaComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            var destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(2L);
            destinatario.setEmail("destinatario@gmail.com");

            var advertencia = criarAdvertencia(usuario, destinatario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findById(advertencia.getId()))
                .thenReturn(Optional.of(advertencia));

            advertenciaService.excluirAdvertencia(advertencia.getId());

            verify(advertenciaRepository).delete(advertencia);
        }

        @Test 
        void devePermitirAdminNaoAvaliadorExcluirAdvertencia() {

            var remetente = usuarioFactoryTest.criarUsuario();
            remetente.setId(2L);
            remetente.setEmail("remetente@gmail.com");
            remetente.setRole(RoleUser.ROLE_FUNCIONARIO);

            var advertencia = criarAdvertencia(remetente, usuario);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findById(advertencia.getId()))
                .thenReturn(Optional.of(advertencia));

            advertenciaService.excluirAdvertencia(advertencia.getId());

            verify(advertenciaRepository).delete(advertencia);
        }
        
        @Test 
        void naoDevePermitirExcluirSeNaoForAdminNemAvaliador() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            var remetente = usuarioFactoryTest.criarUsuario();
            remetente.setId(2L);
            remetente.setEmail("remetente@gmail.com");
            remetente.setRole(RoleUser.ROLE_FUNCIONARIO);

            var destinatario = usuarioFactoryTest.criarUsuario();
            destinatario.setId(3L);
            destinatario.setEmail("destinatario@gmail.com");
    
            var advertencia = criarAdvertencia(remetente, destinatario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(advertenciaRepository.findById(advertencia.getId()))
                .thenReturn(Optional.of(advertencia));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> advertenciaService.excluirAdvertencia(advertencia.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para excluir essa advertência!");

            verify(advertenciaRepository, never()).delete(any(Advertencia.class));
        }

    }

}


