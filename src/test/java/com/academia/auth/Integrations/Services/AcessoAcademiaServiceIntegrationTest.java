package com.academia.auth.Integrations.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaResponseDTO;
import com.academia.auth.Exceptions.AcessoAcademiaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.AdvertenciaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.AcessoAcademiaService;
import com.academia.auth.config.TestContainersConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional
public class AcessoAcademiaServiceIntegrationTest {
    
    @Autowired
    private AcessoAcademiaRepository acessoAcademiaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdvertenciaRepository advertenciaRepository;

    @Autowired
    private AcessoAcademiaService acessoAcademiaService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private MensalidadeRepository mensalidadeRepository;


    private AcessoAcademia criarAcessoAcademia(Usuario usuario) {

        LocalDate hoje = LocalDate.now();

        AcessoAcademia acessoAcademia = AcessoAcademia.builder()
            .diasAcesso(0)
            .inicioSemana(hoje)
            .usuario(usuario)
            .nome(usuario.getNome())
        .build();

        return acessoAcademia;
    }

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("teste@gmail.com");
        usuario.setRole(RoleUser.ROLE_USER);
        usuario.setSenha(passwordEncoder.encode("091812"));

        return usuario;
    }

    private Mensalidade criarMensalidade(Usuario usuario) {

        LocalDate hoje = LocalDate.now();

        Mensalidade m = new Mensalidade();
        m.setValor(BigDecimal.valueOf(45));
        m.setDataCriacao(hoje);
        m.setDataVencimento(hoje.plusMonths(1));
        m.setUsuario(usuario);
        m.setDataPagamento(null);
        m.setDataCancelamento(null);
        m.setDiasTreino(3);
        m.setStatus(StatusMensalidade.PENDENTE);
        m.setAtualizacoes(0);

        return m;
    }

    private Advertencia criarAdvertencia(Usuario remetente, Usuario destinatario) {

        LocalDateTime hoje = LocalDateTime.now();

        Advertencia advertencia = new Advertencia();

        advertencia.setRemetente(remetente);
        advertencia.setDestinatario(destinatario);
        advertencia.setMensagem("Quebrou!");
        advertencia.setNivelAdvertencia(AdvertenciaStatus.LEVE);
        advertencia.setDataCriacao(hoje);
        advertencia.setDataExpiracao(hoje.plusDays(3));

        return advertencia;
    }   

    @Nested
    class acessarAcademiaTest {

        private Usuario aluno;
        private Mensalidade mensalidade;
        private AcessoAcademia acessoAcademia;

        @BeforeEach
        void prepararSetup() {

            aluno = criarUsuario();
            usuarioRepository.save(aluno);

            mensalidade = criarMensalidade(aluno);
            mensalidadeRepository.save(mensalidade);

            acessoAcademia = criarAcessoAcademia(aluno);
            acessoAcademiaRepository.save(acessoAcademia);
        }

        @Test
        void deveAcessarAcademiaComSucesso() {

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                aluno.getEmail(),
                "091812"
            );

            AcessoAcademiaResponseDTO resultado = acessoAcademiaService.acessarAcademia(dto);

            assertThat(resultado.getDiasAcessoSemana()).isEqualTo(1);
            assertThat(resultado.getUltimoAcesso()).isEqualTo(LocalDate.now());
            assertThat(resultado.getRole()).isEqualTo(RoleUser.ROLE_USER);
        }

        @Test
        void deveImpedirAcessoSenhaIncorreta() {

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                aluno.getEmail(),
                "091813"
            );

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> acessoAcademiaService.acessarAcademia(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Senha incorreta!");
        }

        @Test
        void deveImpedirUsuarioNaoAlunoAcessarComoAluno() {

            aluno.setRole(RoleUser.ROLE_FUNCIONARIO);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                aluno.getEmail(),
                "091812"
            );

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.acessarAcademia(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Este acesso é somente para alunos!");
        }

        @Test
        void deveImpedirAcessoMensalidadeEmAtrasoOuCancelada() {

            mensalidade.setStatus(StatusMensalidade.CANCELADA);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                aluno.getEmail(),
                "091812"
            );

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.acessarAcademia(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Mensalidade em atraso ou cancelada!");
        }

        @Test
        void deveImpedirAcessoSeNaoExistirAcessoAcademia() {

            acessoAcademia.setUsuario(null);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                aluno.getEmail(),
                "091812"
            );

            ResourceNotFound except = assertThrows(
                ResourceNotFound.class,
                () -> acessoAcademiaService.acessarAcademia(dto)
            );

            assertThat(except.getMessage()).isEqualTo("Acesso academia não encontrado!");
        }

    }

    @Nested
    class acessarAcademiaFuncionarioTest {

        private Usuario usuario;
        private AcessoAcademia acessoAcademia;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

            acessoAcademia = criarAcessoAcademia(usuario);

            acessoAcademiaRepository.save(acessoAcademia);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveAcessarAcademiaSendoFuncionario() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                usuario.getEmail(),
                "091812"
            );

            AcessoAcademiaResponseDTO resultado = acessoAcademiaService.acessarAcademiaFuncionario(dto);

            assertThat(resultado.getUsuario()).isEqualTo(usuario.getNome());
            assertThat(resultado.getUltimoAcesso()).isEqualTo(LocalDate.now());
        }

        @Test
        void deveImpedirAcessoSenhaIncorretaFuncionario() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                usuario.getEmail(),
                "091813"
            );

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> acessoAcademiaService.acessarAcademiaFuncionario(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Senha incorreta!");
        }

        @Test
        void deveImpedirAcessoUsuarioAlunoComoFuncionario() {

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                usuario.getEmail(),
                "091812"
            );

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.acessarAcademiaFuncionario(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas funcionários podem utlizar esse acesso!");
        }

        @Test
        void deveImpedirSeAcessoNaoEncontradoFuncionario() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            acessoAcademia.setUsuario(null);

            AcessoAcademiaRequestDTO dto = new AcessoAcademiaRequestDTO(
                usuario.getEmail(),
                "091812"
            );

            ResourceNotFound except = assertThrows(
                ResourceNotFound.class,
                () -> acessoAcademiaService.acessarAcademiaFuncionario(dto)
            );

            assertThat(except.getMessage()).isEqualTo("Acesso não encontrado!");
        }

    }   

    @Nested
    class buscarSeuAcessoTest {

        private Usuario usuario;
        private AcessoAcademia acessoAcademia;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

            acessoAcademia = criarAcessoAcademia(usuario);

            acessoAcademiaRepository.save(acessoAcademia);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveBuscarAcessoUsuarioLogado() {

            AcessoAcademiaResponseDTO resultado = acessoAcademiaService.buscarSeuAcesso();
        
            assertThat(resultado.getUsuario()).isEqualTo(usuario.getNome());
            assertThat(resultado.getRole()).isEqualTo(usuario.getRole());
        }

        @Test
        void deveRetornarAcessoNaoEncontradoSeNaoExistir() {

            acessoAcademia.setUsuario(null);

            ResourceNotFound except = assertThrows(
                ResourceNotFound.class,
                () -> acessoAcademiaService.buscarSeuAcesso()
            );

            assertThat(except.getMessage()).isEqualTo("Acesso da academia não encontrado!");
        }

    }

    @Nested
    class buscarTodosAcessoTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

            AcessoAcademia acessoAcademia = criarAcessoAcademia(usuario);

            acessoAcademiaRepository.save(acessoAcademia);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveBuscarTodosAcessosComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Pageable pageable = PageRequest.of(0, 10);

            Page<AcessoAcademiaResponseDTO> resultado = acessoAcademiaService.buscarTodosAcesso(pageable);

            assertThat(resultado.getContent()).extracting(AcessoAcademiaResponseDTO::getUsuario)
                .containsExactlyInAnyOrder(usuario.getNome());

            Page<AcessoAcademia> acessosAcademia = acessoAcademiaRepository.findAll(pageable);

            assertThat(acessosAcademia.getNumberOfElements()).isEqualTo(1);
        }

        @Test
        void deveImpedirUsuarioAlunoVisualizarAcessos() {

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> acessoAcademiaService.buscarTodosAcesso(pageable)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar esses acessos!");
        }

    }

    @Nested
    class buscarAcessoPorNomeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();   
            usuarioRepository.save(usuario);

            AcessoAcademia acessoAcademia = criarAcessoAcademia(usuario);
            acessoAcademiaRepository.save(acessoAcademia);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }
    
        @Test
        void deveBuscarAcessoPorNome() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Pageable pageable = PageRequest.of(0, 10);

            Page<AcessoAcademiaResponseDTO> resultado = acessoAcademiaService.buscarAcessoPorNome(pageable, "sau");
        
            assertThat(resultado.getContent()).extracting(AcessoAcademiaResponseDTO::getUsuario)
                .containsExactlyInAnyOrder(usuario.getNome());

            Page<AcessoAcademia> acessosAcademia = acessoAcademiaRepository.findByNomeContainingIgnoreCase(pageable, "sau");

            assertThat(acessosAcademia.getContent()).extracting(AcessoAcademia::getNome)
                .containsExactlyInAnyOrder(usuario.getNome());
        }
        
        @Test
        void deveImpedirUsuarioAlunoDeBuscarAcessosPorNome() {

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> acessoAcademiaService.buscarAcessoPorNome(pageable, "sau")
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para buscar acessos!");
        }
        
    }

    @Nested
    class validarAdvertenciasAlunoTest {

        private Usuario remetente;
        private Usuario destinatario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            remetente = criarUsuario();

            destinatario = criarUsuario();
            destinatario.setEmail("teste1@gmil.com");

            List<Usuario> usuarios = List.of(remetente, destinatario);

            usuarioRepository.saveAll(usuarios);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                remetente,
                null,
                remetente.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveLiberarAlunoSemAdvertencias() {

            acessoAcademiaService.validarAdvertenciasAluno(destinatario);
        }

        @Test
        void deveImpedirAcessoComUmaAdvertenciaGrave() {

            Advertencia advertencia = criarAdvertencia(remetente, destinatario);
            advertencia.setNivelAdvertencia(AdvertenciaStatus.GRAVE);
            advertenciaRepository.save(advertencia);

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.validarAdvertenciasAluno(destinatario)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode acessar a academia com 1 advertência grave ou mais!");
        }

        @Test
        void deveImpedirAcessoComDuasOuMaisAdvertenciasModeradas() {

            Advertencia advertencia = criarAdvertencia(remetente, destinatario);
            advertencia.setNivelAdvertencia(AdvertenciaStatus.MODERADA);

            Advertencia advertencia2 = criarAdvertencia(remetente, destinatario);
            advertencia2.setNivelAdvertencia(AdvertenciaStatus.MODERADA);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            advertenciaRepository.saveAll(advertencias);

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.validarAdvertenciasAluno(destinatario)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode acessar a academia com 2 advertências moderadas ou mais!");
        }

        @Test
        void deveImpedirAcessoComMaisdeTresAdvertencias() {

            Advertencia advertencia = criarAdvertencia(remetente, destinatario);
            advertencia.setNivelAdvertencia(AdvertenciaStatus.LEVE);

            Advertencia advertencia2 = criarAdvertencia(remetente, destinatario);
            advertencia2.setNivelAdvertencia(AdvertenciaStatus.LEVE);

            Advertencia advertencia3 = criarAdvertencia(remetente, destinatario);
            advertencia.setNivelAdvertencia(AdvertenciaStatus.LEVE);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2, advertencia3);

            advertenciaRepository.saveAll(advertencias);

            AcessoAcademiaException exception = assertThrows(
                AcessoAcademiaException.class,
                () -> acessoAcademiaService.validarAdvertenciasAluno(destinatario)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não pode acessar a academia com 3 advertências ou mais!");
        }

    }

}
