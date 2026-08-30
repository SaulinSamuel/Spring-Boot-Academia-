package com.academia.auth.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.academia.auth.DTOS.Agendamento.AgendamentoResponseDTO;
import com.academia.auth.Exceptions.AgendamentoException;
import com.academia.auth.Exceptions.AulaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Agendamento;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAgendamento;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AgendamentoRepository;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class AgendamentoServiceTest {
    
    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Usuario usuario;

    @BeforeEach
    void configurarUsuario() {

        usuario = Usuario.builder()
            .id(1L)
            .nome("Saulin")
            .telefone("(98) 94512-3422")
            .senha("091812")
            .email("saulo@gmail.com")
            .role(RoleUser.ROLE_USER)
        .build();
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

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setId(2L);
        usuario.setNome("Saulin teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_INSTRUTOR);

        return usuario;
    }

    private Agendamento criarAgendamento(Usuario usuario, Aula aula) {

        LocalDateTime agora = LocalDateTime.now();

        Agendamento agendamento = Agendamento.builder()
            .aula(aula)
            .status(StatusAgendamento.CONFIRMADO)
            .usuario(usuario)
            .dataAgendamento(agora)
        .build();

        return agendamento;
    }

    @Nested
    class criarAgendamentoTest {

        private Aula aula;
        private Usuario instrutor;

        @BeforeEach
        void prepararSetup() {

            instrutor = criarUsuario();

            aula = criarAula(instrutor);
        }

        @Test
        void deveCriarAgendamentoComSucesso() {      

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            when(agendamentoRepository.existeMaisDeUmAgendamentoComStatus(
                usuario.getId(), 
                aula.getId(), 
                StatusAula.PENDENTE))
            .thenReturn(false);

            AgendamentoResponseDTO resultado = agendamentoService.criarAgendamento(aula.getId());

            assertThat(resultado.aluno()).isEqualTo(usuario.getNome());
            assertThat(resultado.status()).isEqualTo(StatusAgendamento.CONFIRMADO);

            verify(agendamentoRepository).save(any(Agendamento.class));
        }

        @Test
        void deveImpedirAgendamentoEmAulaNaoPendente() {

            aula.setStatus(StatusAula.CONCLUIDA);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            AulaException exception = assertThrows(
                AulaException.class,
                () -> agendamentoService.criarAgendamento(aula.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Agendamentos disponíveis apenas para aulas pendentes!");
        
            verify(agendamentoRepository, never()).save(any(Agendamento.class));
        }

        @Test
        void deveImpedirSeExisteAgendamentoEmOutraAulaPendente() {

            Agendamento agendamento = criarAgendamento(usuario, aula);
            agendamento.setAula(aula);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(aulaRepository.findById(aula.getId()))
                .thenReturn(Optional.of(aula));

            when(agendamentoRepository.existeMaisDeUmAgendamentoComStatus(
                usuario.getId(),
                aula.getId(),
                StatusAula.PENDENTE
            ))
            .thenReturn(true);

            AulaException exception = assertThrows(
                AulaException.class,
                () -> agendamentoService.criarAgendamento(aula.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você já tem um agendamento em uma aula pendente!");
        
            verify(agendamentoRepository, never()).save(any(Agendamento.class));
        }

    }

    @Nested
    class buscarTodosAgendamentosTest {

        private Aula aula;

        @BeforeEach
        void prepararSetup() {

            aula = criarAula(usuario);
        }

        @Test
        void deveBuscarTodosAgendamentosComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Agendamento agendamento = criarAgendamento(usuario, aula);

            Agendamento agendamento2 = criarAgendamento(usuario, aula);

            Agendamento agendamento3 = criarAgendamento(usuario, aula);

            List<Agendamento> agendamentos = List.of(agendamento, agendamento2, agendamento3);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Agendamento> page = new PageImpl<>(agendamentos);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(agendamentoRepository.findAll(pageable))
                .thenReturn(page);

            Page<AgendamentoResponseDTO> resultado = agendamentoService.buscarTodosAgendamentos(pageable);

            assertThat(resultado.getContent()).isNotEmpty();
            assertThat(resultado.getContent()).extracting(AgendamentoResponseDTO::id)
                .containsExactlyInAnyOrder(agendamento.getId(), agendamento2.getId(), agendamento3.getId());
        }

        @Test
        void deveImpedirBuscarAgendamentosSemPermissao() {

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> agendamentoService.buscarTodosAgendamentos(pageable)
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de buscar agendamentos!");
        }

    }

    @Nested
    class buscarSeusAgendamentosTest {

        private Aula aula;
        private Usuario instrutor;

        @BeforeEach
        void prepararSetup() {

            instrutor = criarUsuario();
            aula = criarAula(instrutor);
        }

        @Test
        void deveBuscarSeusAgendamentosComSucesso() {

            usuario.setRole(RoleUser.ROLE_USER);

            Agendamento agendamento = criarAgendamento(usuario, aula);

            Agendamento agendamento2 = criarAgendamento(usuario, aula);

            Agendamento agendamento3 = criarAgendamento(usuario, aula);

            List<Agendamento> agendamentos = List.of(agendamento, agendamento2, agendamento3);

            Pageable pageable = PageRequest.of(0, 10);

            Page<Agendamento> page = new PageImpl<>(agendamentos);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(agendamentoRepository.findAllByUsuario(usuario, pageable))
                .thenReturn(page);

            Page<AgendamentoResponseDTO> resultado = agendamentoService.buscarSeusAgendamentos(pageable);

            assertThat(resultado.getContent()).isNotEmpty();
            assertThat(resultado.getContent()).extracting(AgendamentoResponseDTO::id)
                .containsExactlyInAnyOrder(agendamento.getId(), agendamento2.getId(), agendamento3.getId());
        }

    }

    @Nested
    class buscarAgendamentoPorIdTest {

        private Aula aula;
        private Usuario aluno;
        private Agendamento agendamento;

        @BeforeEach
        void prepararSetup() {

            aluno = criarUsuario();
            aluno.setRole(RoleUser.ROLE_USER);
            aula = criarAula(usuario);
            agendamento = criarAgendamento(usuario, aula);
        }

        @Test
        void deveBuscarAgendamentoPorId() {
        
            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(agendamentoRepository.findById(agendamento.getId()))
                .thenReturn(Optional.of(agendamento));

            AgendamentoResponseDTO resultado = agendamentoService.buscarAgendamentoPorId(agendamento.getId());

            assertThat(resultado).isNotNull();
            assertThat(resultado.aluno()).isEqualTo(usuario.getNome());
            assertThat(resultado.id()).isEqualTo(agendamento.getId());
        }   

        @Test
        void deveImpedirVisualizarAgendamentoComUsuarioIdNaoIgual() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(aluno);

            when(agendamentoRepository.findById(agendamento.getId()))
                .thenReturn(Optional.of(agendamento));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> agendamentoService.buscarAgendamentoPorId(agendamento.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar esse agendamento!");
        }

    }

    @Nested
    class cancelarAgendamentoTest {

        private Aula aula;
        private Usuario instrutor;

        @BeforeEach
        void prepararSetup() {

            instrutor = criarUsuario();
            aula = criarAula(instrutor);
        }

        @Test
        void deveCancelarAgendamentoComSucesso() {

            var agendamento = criarAgendamento(usuario, aula);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(agendamentoRepository.findById(agendamento.getId()))
                .thenReturn(Optional.of(agendamento));

            agendamentoService.cancelarAgendamento(agendamento.getId());

            verify(agendamentoRepository).delete(agendamento);
        }

        @Test
        void deveImpedirUsuarioSemPermissao() {

            usuario.setRole(RoleUser.ROLE_INSTRUTOR);
            var agendamento = criarAgendamento(usuario, aula);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> agendamentoService.cancelarAgendamento(agendamento.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de cancelar agendamentos!");
        }

        @Test
        void deveImpedirUsuarioNaoDonoDoAgendamentoCancelar() {

            instrutor.setRole(RoleUser.ROLE_USER);

            var agendamento = criarAgendamento(usuario, aula);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(instrutor);

            when(agendamentoRepository.findById(agendamento.getId()))
                .thenReturn(Optional.of(agendamento));

            AgendamentoException exception = assertThrows(
                AgendamentoException.class,
                () -> agendamentoService.cancelarAgendamento(agendamento.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão de cancelar esse agendamento!");
        }

        @Test
        void deveImpedirCancelarAgendamentoEmAulaNaoPendente() {

            aula.setStatus(StatusAula.CONFIRMADA);
            
            var agendamento = criarAgendamento(usuario, aula);
            
            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(agendamentoRepository.findById(agendamento.getId()))
                .thenReturn(Optional.of(agendamento));

            AulaException exception = assertThrows(
                AulaException.class,
                () -> agendamentoService.cancelarAgendamento(agendamento.getId())
            );

            assertThat(exception.getMessage()).isNotBlank();
            assertThat(exception.getMessage()).isEqualTo("Você não pode cancelar agendamento em aula não pendente!");
        }

    }

}
