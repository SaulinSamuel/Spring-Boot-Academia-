package com.academia.auth.Integrations.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Mensalidade.MensalidadeFilterDatesDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.MensalidadeService;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
public class MensalidadeServiceIntegrationTest {
    
    @Autowired
    private MensalidadeService mensalidadeService;

    @Autowired
    private MensalidadeRepository mensalidadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AcessoAcademiaRepository acessoAcademiaRepository;

    @Test
    void deveSubirContextoDaAplicacao() {
    }

    //helpers
    private Usuario criarUsuario() {
        
        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setRole(RoleUser.ROLE_USER);
        usuario.setSenha("091812");

        return usuario;
    }

    private Mensalidade criarMensalidadePendente(Usuario usuario) {

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

    private MensalidadeRequestDTO criarMensalidadeRequest() {

        MensalidadeRequestDTO dto = new MensalidadeRequestDTO(
            3
        );

        return dto;
    }

    //tests
    @Nested
    class criarMensalidadeTest {

        private Usuario usuario;
        
        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveCriarMensalidadeEAcessoAcademiaNaoExistenteComSucesso() {

            MensalidadeRequestDTO dto = criarMensalidadeRequest();

            MensalidadeResponseDTO resultado = mensalidadeService.criarMensalidade(dto);

            assertThat(resultado).isNotNull();

            Mensalidade mensalidade = mensalidadeRepository.findById(usuario.getId())
                .orElseThrow();

            AcessoAcademia acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario)
                .orElseThrow();

            assertThat(mensalidade.getUsuario()).isEqualTo(usuario);
            assertThat(mensalidade.getDiasTreino()).isEqualTo(dto.getDiasTreino());

            assertThat(acessoAcademia).isNotNull();
            assertThat(acessoAcademia.getUsuario()).isEqualTo(usuario);
        }

        @Test
        void deveCriarMensalidadeAcessoDaAcademiaJaExistente() {

            LocalDate hoje = LocalDate.now();

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            AcessoAcademia acessoAcademia = new AcessoAcademia();
            acessoAcademia.setDiasAcesso(1);
            acessoAcademia.setInicioSemana(hoje);
            acessoAcademia.setUltimoAcesso(hoje);
            acessoAcademia.setUsuario(usuario);
            acessoAcademia.setNome(usuario.getNome());
            usuario.setAcessosAcademia(acessoAcademia);

            acessoAcademiaRepository.save(acessoAcademia);

            MensalidadeRequestDTO dto = criarMensalidadeRequest();

            MensalidadeResponseDTO resultado = mensalidadeService.criarMensalidade(dto);

            assertThat(resultado).isNotNull();

            Mensalidade mensalidadeSalva = mensalidadeRepository.findById(usuario.getId())
                .orElseThrow();

            AcessoAcademia acessoAcademiaUsuario = acessoAcademiaRepository.findByUsuario(usuario)
                .orElseThrow();

            assertThat(mensalidadeSalva).isNotNull();
            assertThat(mensalidadeSalva.getDiasTreino()).isEqualTo(dto.getDiasTreino());

            assertThat(acessoAcademiaUsuario).isNotNull();
            assertThat(acessoAcademiaUsuario.getUsuario()).isEqualTo(acessoAcademia.getUsuario());
        }

        @Test
        void deveLancarExcecaoJaPossuiMensalidadesPendentes() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            MensalidadeRequestDTO dto = criarMensalidadeRequest();

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.criarMensalidade(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você já possui mensalidades pendentes!");
        }

        @Test
        void deveLancarExcecaoJaCancelouMensalidadeEsseMes() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setDataCancelamento(hoje);
            mensalidade.setStatus(StatusMensalidade.CANCELADA);

            mensalidadeRepository.save(mensalidade);

            MensalidadeRequestDTO dto = criarMensalidadeRequest();

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.criarMensalidade(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você já cancelou uma mensalidade esse mês!");
        }

    }

    @Nested
    class atualizarMensalidadeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveAtualizarMensalidadeComSucesso() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            MensalidadeRequestDTO dto = criarMensalidadeRequest();

            MensalidadeResponseDTO resultado = mensalidadeService.atualizarMensalidade(dto);
        
            assertThat(resultado).isNotNull();

            assertThat(resultado.getAluno()).isEqualTo(usuario.getNome());

            Mensalidade mensalidadeAtualizada = mensalidadeRepository.findById(usuario.getId())
                .orElseThrow();

            assertThat(mensalidadeAtualizada.getDiasTreino()).isEqualTo(dto.getDiasTreino());
            assertThat(mensalidadeAtualizada.getAtualizacoes()).isGreaterThan(0);    
        }

        @Test
        void deveLancarExcecaoApenasMensalidadesPendentesPodemSerAlteradas() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.PAGA);
            mensalidade.setDataPagamento(hoje);

            MensalidadeRequestDTO dto =  criarMensalidadeRequest();

            mensalidadeRepository.save(mensalidade);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.atualizarMensalidade(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas mensalidades pendentes podem ser alteradas!");
        }

        @Test
        void deveLancarExcecaoSoPodeAtualizarMensalidadeUmaVezNoMes() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setAtualizacoes(1);

            MensalidadeRequestDTO dto =  criarMensalidadeRequest();

            mensalidadeRepository.save(mensalidade);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.atualizarMensalidade(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Você só pode atualizar sua mensalidade 1 vez por mês!");
        }

    }

    @Nested
    class buscarSuasMensalidadesTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveBuscarSuasMensalidadesComSucesso() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            mensalidadeRepository.saveAll(mensalidades);

            Pageable pageable = PageRequest.of(0, 10);

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarSuasMensalidades(pageable);

            assertThat(resultado).isNotEmpty();

            assertThat(resultado.getContent()).extracting(MensalidadeResponseDTO::getId)
                .containsExactlyInAnyOrder(mensalidade.getId(), mensalidade2.getId());
        }

        @Test
        void deveRetornarVazioSeNaoTemMensalidades() {

            Pageable pageable = PageRequest.of(0, 10);

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarSuasMensalidades(pageable);

            assertThat(resultado).isEmpty();
        }

    }

    @Nested
    class buscarTodasMensalidadesComFiltroTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveBuscarTodasMensalidadesComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
            mensalidade2.setDataCriacao(hoje.plusDays(1));

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            mensalidadeRepository.saveAll(mensalidades);

            Pageable pageable = PageRequest.of(0, 10);

            BigDecimal valor = BigDecimal.valueOf(45);
            Integer diasTreino = 3;
            MensalidadeFilterDatesDTO filterDatesDTO = new MensalidadeFilterDatesDTO(
                null, 
                hoje, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null
            );

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarTodasMensalidadesComFiltro(
                valor, 
                diasTreino, 
                filterDatesDTO, 
                pageable
            );

            assertThat(resultado).isNotEmpty();

            assertThat(resultado.getContent()).extracting(MensalidadeResponseDTO::getId)
                .containsExactly(mensalidade.getId());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaVisualizarTodasMensalidades() {

            Pageable pageable = PageRequest.of(0, 10);

            BigDecimal valor = BigDecimal.valueOf(45);
            Integer diasTreino = 3;
            MensalidadeFilterDatesDTO filterDatesDTO = new MensalidadeFilterDatesDTO(
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null
            );

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.buscarTodasMensalidadesComFiltro(valor, diasTreino, filterDatesDTO, pageable)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar as mensalidades!");
        }

    }
    
    @Nested
    class buscarMensalidadesPorNomeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveBuscarMensalidadesPorNomeComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
            
            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            mensalidadeRepository.saveAll(mensalidades);

            Pageable pageable = PageRequest.of(0, 10);

            String nome = "saul";

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarMensalidadesPorNome(pageable, nome);

            assertThat(resultado).isNotEmpty();

            assertThat(resultado.getContent()).extracting(MensalidadeResponseDTO::getId)
                .containsExactlyInAnyOrder(mensalidade.getId(), mensalidade2.getId());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaVisualizarOutrasMensalidades() {

            Pageable pageable = PageRequest.of(0, 10);

            String nome = "sau";

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.buscarMensalidadesPorNome(pageable, nome)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar outras mensalidades!");
        }

    }

    @Nested
    class pagarMensalidadeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void devePagarMensalidadeComSucesso() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            MensalidadeResponseDTO resultado= mensalidadeService.pagarMensalidade();

            LocalDate hoje = LocalDate.now();

            assertThat(resultado).isNotNull();

            assertThat(resultado.getStatus()).isEqualTo(StatusMensalidade.PAGA);
            assertThat(resultado.getDataPagamento()).isEqualTo(hoje);
        }   

        @Test
        void deveLancarExcecaoApenasMensalidadesPendentesOuAtrasadasPodemSerPagas() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.CANCELADA);
            mensalidade.setDataCancelamento(hoje);

            mensalidadeRepository.save(mensalidade);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.pagarMensalidade()
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas mensalidades pendentes(ou atrasadas) podem ser pagas!");
        }

    }

    @Nested
    class cancelarMensalidadeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveCancelarMensalidadeDeAlunoComSucesso() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            LocalDate hoje = LocalDate.now();

            AcessoAcademia acessoAcademia = new AcessoAcademia();
            acessoAcademia.setDiasAcesso(1);
            acessoAcademia.setInicioSemana(hoje);
            acessoAcademia.setUltimoAcesso(hoje);
            acessoAcademia.setUsuario(usuario);
            acessoAcademia.setNome(usuario.getNome());

            mensalidadeRepository.save(mensalidade);

            acessoAcademiaRepository.save(acessoAcademia);

            MensalidadeResponseDTO resultado = mensalidadeService.cancelarMensalidade();

            assertThat(resultado.getStatus()).isEqualByComparingTo(StatusMensalidade.CANCELADA);

            Optional<AcessoAcademia> acessoAcademiaDeletado = acessoAcademiaRepository.findByUsuario(usuario);

            assertThat(acessoAcademiaDeletado).isEmpty();
        }

        @Test
        void deveCancelarMensalidadeDeFuncionarioComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            MensalidadeResponseDTO resultado = mensalidadeService.cancelarMensalidade();

            assertThat(resultado.getStatus()).isEqualTo(StatusMensalidade.CANCELADA);
        }

        @Test
        void deveLancarExcecaoApenasMensalidadesPendentesPodemSerCanceladas() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.ATRASADA);

            mensalidadeRepository.save(mensalidade);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.cancelarMensalidade()
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas mensalidades pendentes podem ser canceladas!");
        }

    }

    @Nested
    class excluirMensalidadeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);

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
        void deveExcluirMensalidadeComSucesso() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.PAGA);
            mensalidade.setDataPagamento(hoje);

            mensalidadeRepository.save(mensalidade);

            mensalidadeService.excluirMensalidade();

            Optional<Mensalidade> mensalidadeExcluida = mensalidadeRepository.findTopByUsuarioOrderByDataCriacaoDesc(usuario);

            assertThat(mensalidadeExcluida).isEmpty();
        }

        @Test
        void deveLancarExcecaoApenasMensalidadesPagasOuCanceladasPodemSerExcluidas() {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.excluirMensalidade()
            );

            assertThat(exception.getMessage()).isEqualTo("Apenas mensalidades pagas(ou canceladas) podem ser excluídas!");
        }

    }

}
