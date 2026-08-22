package com.academia.auth.Integrations.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.AcessoAcademiaService;
import com.academia.auth.config.TestContainersConfig;

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

    @Nested
    class acessarAcademiaTest {

        private Usuario usuario;
        private Usuario aluno;
        private Mensalidade mensalidade;
        private AcessoAcademia acessoAcademia;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            usuarioRepository.save(aluno);

            mensalidade = criarMensalidade(aluno);
            mensalidadeRepository.save(mensalidade);

            acessoAcademia = criarAcessoAcademia(aluno);
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

}
