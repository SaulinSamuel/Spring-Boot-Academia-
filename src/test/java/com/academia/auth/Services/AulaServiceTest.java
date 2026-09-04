package com.academia.auth.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.Aula.AulaRequestDTO;
import com.academia.auth.DTOS.Aula.AulaResponseDTO;
import com.academia.auth.Exceptions.AulaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class AulaServiceTest {
    
    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @InjectMocks
    private AulaService aulaService;

    private Usuario usuario;

    @BeforeEach
    void configure() { 

        usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_INSTRUTOR);
    }

    private Aula criarAula(Usuario instrutor) {

        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.of(13, 30);
        LocalTime fim = inicio.plusHours(1);

        Aula aula = Aula.builder()
            .capacidadeInscricoes(10)
            .id(1L)
            .dataAula(hoje)
            .horarioInicio(inicio)
            .horarioFim(fim)
            .instrutor(instrutor)
            .status(StatusAula.PENDENTE)
            .nome("Aula treino inferiores")
        .build();

        return aula;
    }

    private AulaRequestDTO criarAulaRequest() {
    
        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.of(13, 30);
        LocalTime fim = inicio.plusHours(1);

        AulaRequestDTO dto = new AulaRequestDTO(
            "Aula superiores",
            hoje,
            inicio,
            fim,
            11
        );

        return dto;
    }

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setId(2L);
        usuario.setNome("Saulin teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_INSTRUTOR);

        return usuario;
    }

    @Nested
    class criarAulaTest {

        @Test
        void deveCriarAulaComSucesso() {

            AulaRequestDTO dto = criarAulaRequest();

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);
            
            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.empty());
            
            AulaResponseDTO resultado = aulaService.criarAula(dto);

            assertThat(resultado.instrutor()).isEqualTo(usuario.getNome());

            verify(aulaRepository).save(any(Aula.class));
        }

        @Test
        void deveImpedirCriarAulaUsuarioSemPermissao() {

            AulaRequestDTO dto = criarAulaRequest();

            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.criarAula(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para criar aulas!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

        @Test
        void deveImpedirCriarAulaComUmaJaExistentePorStatus() {

            AulaRequestDTO dto = criarAulaRequest();

            Aula aula = criarAula(usuario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.criarAula(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode criar aulas enquanto não concluir atual!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }   

    }

    @Nested
    class atualizarAulaTest {

        @Test
        void deveAtualizarAulaComSucesso() {

            AulaRequestDTO dto = criarAulaRequest();

            Aula aula = criarAula(usuario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            AulaResponseDTO resultado = aulaService.atualizarAula(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.capacidadeInscricoes()).isEqualTo(dto.capacidadeInscricoes());
            assertThat(resultado.nome()).isEqualTo(dto.nome());
            assertThat(resultado.dataAula()).isEqualTo(dto.dataAula());
            assertThat(resultado.horarioInicio()).isEqualTo(dto.horarioInicio());
            assertThat(resultado.horarioFim()).isEqualTo(dto.horarioFim());

            verify(aulaRepository).save(any(Aula.class));
        }

        @Test
        void deveImpedirAtualizarAulaUsuarioSemPermissao() {

            AulaRequestDTO dto = criarAulaRequest();

            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.atualizarAula(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para criar aulas!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

        @Test
        void deveImpedirAtualizarAulaNaoPendente() {

            AulaRequestDTO dto = criarAulaRequest();

            Aula aula = criarAula(usuario);
            aula.setStatus(StatusAula.CONCLUIDA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aulaService.atualizarAula(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode atualizar aulas não pendentes!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

    }

    @Nested
    class confirmarAulaTest {

        @Test
        void deveConfirmarAulaComSucesso() {

            Aula aula = criarAula(usuario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));
            
            AulaResponseDTO resultado = aulaService.confirmarAula();

            assertThat(resultado.status()).isEqualTo(StatusAula.CONFIRMADA);

            verify(aulaRepository).save(any(Aula.class));
        }

        @Test
        void deveImpedirConfirmarAulaUsuarioSemPermissao() {

            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.confirmarAula()
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para confirmar aulas!");
        
            verify(aulaRepository, never()).save(any(Aula.class));
        }

        @Test
        void deveImpedirAtualizarAulasNaoPendentes() {

            Aula aula = criarAula(usuario);
            aula.setStatus(StatusAula.CANCELADA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.confirmarAula()
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas aulas pendentes podem ser confirmadas!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

    }

    @Nested
    class cancelarAulaTest {

        @Test
        void deveCancelarAulaComSucesso() {

            Aula aula = criarAula(usuario);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            AulaResponseDTO resultado = aulaService.cancelarAula();

            assertThat(resultado.status()).isEqualTo(StatusAula.CANCELADA);

            verify(aulaRepository).save(any(Aula.class));
        }

        @Test
        void deveImpedirCancelarAulaUsuarioSemPermissao() {

            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.cancelarAula()
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para cancelar aulas!");
        
            verify(aulaRepository, never()).save(any(Aula.class));
        }

        @Test
        void deveImpedirCancelarAulaNaoPendente() {

            Aula aula = criarAula(usuario);
            aula.setStatus(StatusAula.CANCELADA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.of(aula));

            AulaException exception = assertThrows(
                AulaException.class,
                () -> aulaService.cancelarAula()
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas aulas pendentes podem ser canceladas!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

    }
    
    @Nested 
    class buscarTodasAulasTest {

        @Test
        void deveBuscarTodasAulas() {

            Pageable pageable = PageRequest.of(0, 10);

            Usuario usuario2 = criarUsuario();

            Aula aula = criarAula(usuario);

            Aula aula2 = criarAula(usuario2);

            List<Aula> aulas = List.of(aula, aula2);

            Page<Aula> page = new PageImpl<>(aulas);

            when(aulaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

            Page<AulaResponseDTO> resultado = aulaService.buscarTodasAulas(null, pageable);

            assertThat(resultado.getContent()).extracting(AulaResponseDTO::id)
                .containsExactlyInAnyOrder(aula.getId(), aula2.getId());
        }

    }

    @Nested
    class buscarAulasCriadasPorInstrutorTest {

        @Test
        void deveBuscarAulasCriadasPorInstrutorComSucesso() {

            Pageable pageable = PageRequest.of(0, 10);

            Usuario instrutor = criarUsuario();
            instrutor.setEmail("instrutor@gmail.com");

            Aula aula = criarAula(instrutor);
            
            Aula aula2 = criarAula(instrutor);
            aula2.setStatus(StatusAula.CONCLUIDA);

            List<Aula> aulas = List.of(aula, aula2);

            Page<Aula> page = new PageImpl<>(aulas);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(instrutor);

            when(aulaRepository.findAllByInstrutor(instrutor, pageable))
                .thenReturn(page);

            Page<AulaResponseDTO> resultado = aulaService.buscarAulasCriadasPorInstrutor(pageable);

            assertThat(resultado.getContent()).extracting(AulaResponseDTO::id)
                .containsExactlyInAnyOrder(aula.getId(), aula2.getId());
        }

        @Test
        void deveImpedirBuscarAulasSemPermissao() {
            
            Pageable pageable = PageRequest.of(0, 10);
            
            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aulaService.buscarAulasCriadasPorInstrutor(pageable)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de visualizar aulas!");

            verify(aulaRepository, never()).save(any(Aula.class));
        }

    }

    @Nested 
    class buscarAulaPorIdTest {

        @Test
        void deveBuscarAulaPorIdComSucesso() {

            Aula aula = criarAula(usuario);
        
            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            AulaResponseDTO resultado = aulaService.buscarAulaPorId(aula.getId());

            assertThat(resultado.id()).isEqualTo(aula.getId());
        }

    }

    @Nested
    class excluirAulaTest {

        @Test
        void deveExcluirAulaComSucesso() {

            Aula aula = criarAula(usuario);
            aula.setStatus(StatusAula.CONCLUIDA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            aulaService.excluirAula(aula.getId());

            verify(aulaRepository).delete(aula);

            Optional<Aula> aulaExcluida = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario);
            
            assertThat(aulaExcluida).isEmpty();
        }

        @Test
        void deveImpedirExcluirAulaUsuarioSemPermissao() {

            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aulaService.excluirAula(1L)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para excluir aulas!");

            verify(aulaRepository, never()).delete(any(Aula.class));
        }

        @Test
        void deveImpedirExcluirAulaPorUsuarioIdIncorreto() {

            Aula aula = criarAula(usuario);

            Usuario usuarioNaoDono = criarUsuario();

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuarioNaoDono);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            BusinessException exception = assertThrows(
                BusinessException.class, 
                () -> aulaService.excluirAula(aula.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de excluir essa aula!");

            verify(aulaRepository, never()).delete(any(Aula.class));
        }

        @Test
        void deveImpedirExcluirAulaPorAulaStatusPendente() {

            Aula aula = criarAula(usuario);
            aula.setStatus(StatusAula.CONFIRMADA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aulaService.excluirAula(aula.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas aulas concluidas(ou canceladas) podem ser excluidas!");

            verify(aulaRepository, never()).delete(any(Aula.class));
        }

    }

}
