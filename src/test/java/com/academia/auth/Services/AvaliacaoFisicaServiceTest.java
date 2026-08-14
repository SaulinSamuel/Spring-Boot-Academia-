package com.academia.auth.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoRequestDTO;
import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.AvaliacaoFisica;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AvaliacaoFisicaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoFisicaServiceTest {

    @Mock
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @InjectMocks
    private AvaliacaoFisicaService avaliacaoFisicaService;

    private Usuario usuario;

    @BeforeEach
    void configure() { 

        usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);
    }

    //helpers
    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);

        return usuario;
    }

    private AvaliacaoRequestDTO criarAvaliacaoRequest() {

        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
        dto.setAltura(1.78);
        dto.setBraco(0.42);
        dto.setCintura(0.98);
        dto.setIdade(17);
        dto.setMassaMuscular(45.86);
        dto.setPeito(1.73);
        dto.setPercentualGordura(20.52);
        dto.setPeso(90.3);

        return dto;
    }

    private AvaliacaoFisica criarAvaliacaoFisica(Usuario aluno) {

        AvaliacaoFisica avaliacaoFisica = new AvaliacaoFisica();

        avaliacaoFisica.setAltura(1.97);
        avaliacaoFisica.setBraco(0.41);
        avaliacaoFisica.setCintura(0.77);
        avaliacaoFisica.setIdade(42);
        avaliacaoFisica.setMassaMuscular(18.4);
        avaliacaoFisica.setPeito(0.21);
        avaliacaoFisica.setPercentualGordura(15.31);
        avaliacaoFisica.setPeso(71.6);
        avaliacaoFisica.setAluno(aluno);

        return avaliacaoFisica;
    }

    @Nested
    class criarAvaliacaoFisicaTest {

        @Test
        void deveCriarAvaliacaoFisicaComSucesso() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = hoje.withDayOfMonth(1);
            LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

            AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
            dto.setAltura(1.78);
            dto.setBraco(0.42);
            dto.setCintura(0.98);
            dto.setIdade(17);
            dto.setMassaMuscular(45.86);
            dto.setPeito(1.73);
            dto.setPercentualGordura(20.52);
            dto.setPeso(90.3);

            when(avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
                aluno, 
                inicioMes, 
                fimMes))
            .thenReturn(false);

            when(usuarioRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(aluno));

            AvaliacaoResponseDTO resultado = avaliacaoFisicaService.criarAvaliacaoFisica(dto, aluno.getId());
        
            assertNotNull(resultado);

            assertEquals(dto.getPeso(), resultado.getPeso());
            assertEquals(usuario.getNome(), resultado.getAvaliador());
            assertEquals(aluno.getNome(), resultado.getAluno());

            ArgumentCaptor<AvaliacaoFisica> captor = ArgumentCaptor.forClass(AvaliacaoFisica.class);

            verify(avaliacaoFisicaRepository).save(captor.capture());

            AvaliacaoFisica avaliacaoFisicaCapturada = captor.getValue();

            assertEquals(usuario, avaliacaoFisicaCapturada.getAvaliador());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaCriarAvaliacoesFisicas() {

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.criarAvaliacaoFisica(dto, 2L)
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para criar avaliações físicas!");

            verify(avaliacaoFisicaRepository, never()).save(any(AvaliacaoFisica.class));
        }

        @Test
        void deveLancarExcecaoNaoEPermitidoFazerAvaliacaoFisicaEmFuncionarios() {

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            Usuario aluno = criarUsuario();
            aluno.setRole(RoleUser.ROLE_FUNCIONARIO);

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(aluno));

            BusinessException exception = assertThrows(
                BusinessException.class,
                ()-> avaliacaoFisicaService.criarAvaliacaoFisica(dto, aluno.getId())
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Não é permitido fazer avaliações físicas em funcionários!");

            verify(avaliacaoFisicaRepository, never()).save(any(AvaliacaoFisica.class));
        }

        @Test
        void deveLancarExcecaoAvaliacaoFisicaPodeSerFeitaApenasUmaVezNoMes() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = hoje.withDayOfMonth(1);
            LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

            Usuario aluno = criarUsuario();

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setDataAvaliacao(hoje);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(aluno));

            when(avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(aluno, 
                inicioMes, 
                fimMes)
            ).thenReturn(true);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.criarAvaliacaoFisica(dto, aluno.getId())
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Avaliação física só pode ser feita uma vez no mês por aluno!");
        
            verify(avaliacaoFisicaRepository, never()).save(any(AvaliacaoFisica.class));
        }

    }

    @Nested
    class editarAvaliacaoFisicaTest {

        @Test
        void deveEditarAvaliacaoFisicaComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(usuario);
            avaliacaoFisica.setAluno(aluno);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            AvaliacaoResponseDTO resultado = avaliacaoFisicaService.editarAvaliacaoFisica(
                dto,
                aluno.getId()
            );

            assertNotNull(resultado);

            assertThat(resultado.getAltura()).isEqualTo(dto.getAltura());
            assertThat(resultado.getCintura()).isEqualTo(dto.getCintura());

            ArgumentCaptor<AvaliacaoFisica> captor = ArgumentCaptor.forClass(AvaliacaoFisica.class);

            verify(avaliacaoFisicaRepository).save(captor.capture());

            AvaliacaoFisica avaliacaoFisicaCapturada = captor.getValue();

            assertThat(avaliacaoFisicaCapturada.getId()).isEqualTo(avaliacaoFisica.getId());
        } 
    
        @Test
        void deveEditarAvaliacaoFisicaComSucessoSendoAdmin() {

            usuario.setRole(RoleUser.ROLE_ADMIN);

            Usuario aluno = criarUsuario();
            Usuario avaliador = criarUsuario();
            avaliador.setRole(RoleUser.ROLE_FUNCIONARIO);

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(avaliador);
            avaliacaoFisica.setAluno(aluno);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            AvaliacaoResponseDTO resultado = avaliacaoFisicaService.editarAvaliacaoFisica(
                dto,
                aluno.getId()
            );

            assertNotNull(resultado);

            assertThat(resultado.getAltura()).isEqualTo(dto.getAltura());
            assertThat(resultado.getCintura()).isEqualTo(dto.getCintura());

            ArgumentCaptor<AvaliacaoFisica> captor = ArgumentCaptor.forClass(AvaliacaoFisica.class);

            verify(avaliacaoFisicaRepository).save(captor.capture());

            AvaliacaoFisica avaliacaoFisicaCapturada = captor.getValue();

            assertThat(avaliacaoFisicaCapturada.getId()).isEqualTo(avaliacaoFisica.getId());
        }
        
        @Test   
        void deveLancarExcecaoSemPermissaoParaEditarAvaliacoesSendoUser() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.editarAvaliacaoFisica(dto, 1L)
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para editar avaliações fisícas!");

            verify(avaliacaoFisicaRepository, never()).save(any(AvaliacaoFisica.class));
        }

        @Test
        void deveLancarExcecaoVoceNaoTemPermissaoParaEditarEssaAvaliacaoFisica() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario avaliadorReal = criarUsuario();
            avaliadorReal.setId(3L);
            avaliadorReal.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            AvaliacaoRequestDTO dto = criarAvaliacaoRequest();

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setId(2L);
            avaliacaoFisica.setAvaliador(avaliadorReal);
            avaliacaoFisica.setAluno(aluno);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(avaliacaoFisica.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.editarAvaliacaoFisica(dto, avaliacaoFisica.getId())
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para editar essa avaliação física!");

            verify(avaliacaoFisicaRepository, never()).save(any(AvaliacaoFisica.class));
        }

    }

    @Nested
    class buscarSuasAvaliacaoFisicaAlunosTest {

        @Test
        void deveBuscarAvaliacoesFisicasPageadasComSucesso() {

            usuario.setRole(RoleUser.ROLE_USER);

            Usuario avaliador = criarUsuario();
            avaliador.setRole(RoleUser.ROLE_FUNCIONARIO);

            Pageable pageable = PageRequest.of(0, 10);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(usuario);
            avaliacaoFisica.setAvaliador(avaliador);

            AvaliacaoFisica avaliacaoFisica2 = criarAvaliacaoFisica(usuario);
            avaliacaoFisica2.setAvaliador(avaliador);

            List<AvaliacaoFisica> avaliacaoFisicas = List.of(avaliacaoFisica, avaliacaoFisica2);

            Page<AvaliacaoFisica> page = new PageImpl<>(avaliacaoFisicas);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findAllByAluno(usuario, pageable))
                .thenReturn(page);

            Page<AvaliacaoResponseDTO> resultado = avaliacaoFisicaService.buscarSuasAvaliacaoFisicaAlunos(pageable);

            assertNotNull(resultado);

            assertThat(resultado.getContent()).extracting(AvaliacaoResponseDTO::getId)
                .containsExactlyInAnyOrder(avaliacaoFisica.getId(), avaliacaoFisica2.getId());
        }

        @Test
        void deveLancarExcecaoApenasAlunosPodemVisualizarSuasAvaliacoesFisicasAqui() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.buscarSuasAvaliacaoFisicaAlunos(pageable)
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Apenas alunos podem visualizar suas avaliações físicas por aqui!");
        }

    }

    @Nested
    class buscarSuasAvaliacoesFisicasCriadasTest {

        @Test
        void deveBuscarSuasAvaliacoesFisicasCriadascomSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            Pageable pageable = PageRequest.of(0, 10);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(usuario);

            AvaliacaoFisica avaliacaoFisica2 = criarAvaliacaoFisica(aluno);
            avaliacaoFisica2.setAvaliador(usuario);

            List<AvaliacaoFisica> avaliacaoFisicas = List.of(avaliacaoFisica, avaliacaoFisica2);

            Page<AvaliacaoFisica> page = new PageImpl<>(avaliacaoFisicas);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findAllByAvaliador(usuario, pageable))
                .thenReturn(page);

            Page<AvaliacaoResponseDTO> resultado = avaliacaoFisicaService.buscarSuasAvaliacoesFisicasCriadas(pageable);

            assertNotNull(resultado);

            assertThat(resultado.getContent()).extracting(AvaliacaoResponseDTO::getId)
                .containsExactlyInAnyOrder(avaliacaoFisica.getId(), avaliacaoFisica2.getId());
        }

        @Test
        void deveLancarExcecaoApenasFuncionariosEAdminsPodemVisualizarAvaliacoesPorAqui() {

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.buscarSuasAvaliacoesFisicasCriadas(pageable)
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Apenas funcionários e admins podem ver suas avaliações físicas criadas!");
        }

    }   

    @Nested
    class buscarTodasAvaliacoesFisicasPorFiltroTest {

        @Test
        void deveBuscarTodasAvaliacoesFisicasPorFiltroComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            Pageable pageable = PageRequest.of(0, 10);

            String nomeAluno = "João";
            String avaliador = "Carlos";
            Integer idade = 20;
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 8, 13);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(usuario);

            AvaliacaoFisica avaliacaoFisica2 = criarAvaliacaoFisica(aluno);
            avaliacaoFisica2.setAvaliador(usuario);

            List<AvaliacaoFisica> avaliacaoFisicas = List.of(avaliacaoFisica, avaliacaoFisica2);

            Page<AvaliacaoFisica> page = new PageImpl<>(avaliacaoFisicas);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

            Page<AvaliacaoResponseDTO> resultado = avaliacaoFisicaService.buscarTodasAvaliacoesFisicasPorFiltro(
                nomeAluno, 
                avaliador, 
                idade, 
                inicio, 
                fim, 
                pageable
            );

            assertNotNull(resultado);

            assertThat(resultado.getContent()).extracting(AvaliacaoResponseDTO::getId)
                .containsExactlyInAnyOrder(avaliacaoFisica.getId(), avaliacaoFisica2.getId());
        }

        void deveLancarExcecaoSemPermissaoParaVisualizarTodasAsAvaliacoes() {

            Pageable pageable = PageRequest.of(0, 10);

            String nomeAluno = "João";
            String avaliador = "Carlos";
            Integer idade = 20;
            LocalDate inicio = LocalDate.of(2026, 1, 1);
            LocalDate fim = LocalDate.of(2026, 8, 13);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> avaliacaoFisicaService.buscarTodasAvaliacoesFisicasPorFiltro(
                    nomeAluno, 
                    avaliador, 
                    idade, 
                    inicio, 
                    fim, 
                    pageable
                )
            );

            assertNotNull(exception);

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar todas as avaliações!");
        }

    }

    @Nested
    class buscarAvaliacaoFisicaPorIdTest {

        @Test
        void deveBuscarAvaliacoesFisicasPorIdComSucesso() {

            Usuario aluno = criarUsuario();

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(usuario);
            avaliacaoFisica.setId(2L);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(avaliacaoFisica.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            AvaliacaoResponseDTO resultado = avaliacaoFisicaService.buscarAvaliacaoFisicaPorId(avaliacaoFisica.getId());

            assertNotNull(resultado);

            assertThat(resultado.getId()).isEqualTo(avaliacaoFisica.getId());
            assertThat(resultado.getAluno()).isEqualTo(aluno.getNome());
        }

    }

    @Nested
    class excluirAvaliacaoFisicaTest {

        @Test
        void deveExcluirAvaliacaoFisicaSendoAvaliadorComSucesso() {

            Usuario aluno = criarUsuario();

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(usuario);
            avaliacaoFisica.setId(2L);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(avaliacaoFisica.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            avaliacaoFisicaService.excluirAvaliacaoFisica(avaliacaoFisica.getId());

            verify(avaliacaoFisicaRepository).delete(avaliacaoFisica);
        }

        @Test
        void deveExcluirAvaliacaoFisicaSendoAdminENaoAvaliadorComSucesso() {

            Usuario aluno = criarUsuario();

            Usuario avaliadorReal = criarUsuario();
            avaliadorReal.setRole(RoleUser.ROLE_FUNCIONARIO);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setAvaliador(avaliadorReal);
            avaliacaoFisica.setId(2L);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(avaliacaoFisica.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            avaliacaoFisicaService.excluirAvaliacaoFisica(avaliacaoFisica.getId());

            verify(avaliacaoFisicaRepository).delete(avaliacaoFisica);
        }

        @Test
        void deveLancarExcecaoVoceNaoTemPermissaoParaExcluirEssaAvaliacaoFisica() {

            Usuario aluno = criarUsuario();
            aluno.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario avaliadorReal = criarUsuario();

            AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
            avaliacaoFisica.setId(2L);
            avaliacaoFisica.setAvaliador(avaliadorReal);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(avaliacaoFisicaRepository.findById(avaliacaoFisica.getId()))
                .thenReturn(Optional.of(avaliacaoFisica));

            avaliacaoFisicaService.excluirAvaliacaoFisica(avaliacaoFisica.getId());

            verify(avaliacaoFisicaRepository).delete(avaliacaoFisica);
        }

    }

}
