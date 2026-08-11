package com.academia.auth.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class MensalidadeServiceTest {
    
    @Mock
    private MensalidadeRepository mensalidadeRepository;                 

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @Mock
    private AcessoAcademiaRepository academiaRepository;

    private Usuario usuario;

    @InjectMocks
    private MensalidadeService mensalidadeService;

    @BeforeEach
    void configure() {

        usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);
    }

    private Mensalidade criarMensalidadePendente(Usuario usuario) {

        LocalDate hoje = LocalDate.now();

        Mensalidade m = new Mensalidade();
        m.setValor(BigDecimal.valueOf(50));
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

    private AcessoAcademia criarAcessoAcademia(Usuario usuario) {

        LocalDate hoje = LocalDate.now();

        AcessoAcademia acessoAcademia = new AcessoAcademia();
        acessoAcademia.setDiasAcesso(0);
        acessoAcademia.setId(2L);
        acessoAcademia.setInicioSemana(hoje);
        acessoAcademia.setNome(usuario.getNome());
        acessoAcademia.setUltimoAcesso(hoje);

        return acessoAcademia;
    }

    @Nested
    class criarMensalidadeTest {

        @Test
        void deveCriarMensalidadeComSucesso() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            when(mensalidadeRepository.existsByUsuarioAndStatus(
                usuario,
                StatusMensalidade.PENDENTE))
            .thenReturn(false);

            when(mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween(
                eq(usuario),
                any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(false);

            MensalidadeResponseDTO resultado = mensalidadeService.criarMensalidade(dto);

            assertNotNull(resultado);
            assertEquals(dto.getDiasTreino(), resultado.getDiasTreino());

            verify(mensalidadeRepository).save(any(Mensalidade.class));
            verify(academiaRepository).save(any(AcessoAcademia.class));
        }

        @Test
        void deveLancarExcecaoVoceJaCancelouMensalidadeEsseMes() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            when(mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween(
                eq(usuario), 
                any(LocalDate.class), 
                any(LocalDate.class)))
            .thenReturn(true);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.criarMensalidade(dto)
            );

            assertEquals("Você já cancelou uma mensalidade esse mês!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(any(Mensalidade.class));
        }
    
        @Test
        void deveLancarExcecaoVocePossuiMensalidadesPendentes() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            when(mensalidadeRepository.existsByUsuarioAndStatus(
                usuario, 
                StatusMensalidade.PENDENTE))
            .thenReturn(true);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.criarMensalidade(dto)
            );

            assertEquals("Você já possui mensalidades pendentes!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(any(Mensalidade.class));
        }
    }  

    @Nested
    class atualizarMensalidadeTest {

        @Test
        void deveAtualizarMensalidadeComSucesso() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            when(mensalidadeRepository.save(any(Mensalidade.class)))
                .thenReturn(mensalidade);

            MensalidadeResponseDTO resultado = mensalidadeService.atualizarMensalidade(dto);

            assertNotNull(resultado);

            assertEquals(mensalidade.getDiasTreino(), resultado.getDiasTreino());
            assertEquals(mensalidade.getDataCriacao(), resultado.getDataCriacao());
            assertEquals(mensalidade.getId(), resultado.getId());

            verify(mensalidadeRepository).save(mensalidade);
        }

        @Test
        void deveLancarExcecaoMensalidadeNaoEncontrada() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.empty());

            ResourceNotFound ex = assertThrows(
                ResourceNotFound.class,
                () -> mensalidadeService.atualizarMensalidade(dto)
            );

            assertEquals("Mensalidade não encontrada!", ex.getMessage());

            verify(mensalidadeRepository, never()).save(any(Mensalidade.class));
        }
    
        @Test
        void deveLancarExcecaoApenasMensalidadesPendentesPodemSerAlteradas() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            LocalDate hoje = LocalDate.now();

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidade.setDataPagamento(hoje);
            mensalidade.setStatus(StatusMensalidade.PAGA);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.atualizarMensalidade(dto)
            );

            assertEquals("Apenas mensalidades pendentes podem ser alteradas!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(mensalidade);
        }
        
        @Test
        void deveLancarExcecaoPodeAtualizarMensalidadeUmaVezNoMes() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
  
            mensalidade.setAtualizacoes(1);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.atualizarMensalidade(dto)
            );

            assertEquals("Você só pode atualizar sua mensalidade 1 vez por mês!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(mensalidade);
        }
    }

    @Nested
    class buscarSuasMensalidadesTest {

        @Test
        void deveBuscarSuasMensalidadesComSucesso() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
            mensalidade.setId(2L);

            Pageable pageable = PageRequest.of(0, 10);
            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            Page<Mensalidade> page = new PageImpl<>(mensalidades);

            when(mensalidadeRepository.findAllByUsuario(usuario, pageable))
                .thenReturn(page);

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarSuasMensalidades(pageable);

            assertNotNull(resultado);

            assertEquals(mensalidade.getId(), resultado.getContent().get(0).getId());

            assertEquals(page.getTotalPages(), resultado.getTotalPages());
            assertEquals(page.getSize(), resultado.getSize());
            
        }

        @Test
        void deveLancarExcecaoDeMensalidadesNaoEncontradas() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            Pageable pageable = PageRequest.of(0, 10);

            List<Mensalidade> mensalidades = List.of();

            Page<Mensalidade> page = new PageImpl<>(mensalidades);

            when(mensalidadeRepository.findAllByUsuario(usuario, pageable))
                .thenReturn(page);

            ResourceNotFound ex = assertThrows(
                ResourceNotFound.class,
                () -> mensalidadeService.buscarSuasMensalidades(pageable)
            );

            assertEquals("Mensalidades não encontradas!", ex.getMessage());
        }
        
    }

    @Nested
    class buscarTodasMensalidadesTest {

        @Test
        void deveBuscarTodasMensalidadesComSucesso() {
            
            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
            mensalidade2.setId(2L);

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Mensalidade> page = new PageImpl<>(mensalidades);

            when(mensalidadeRepository.findAll(pageable))
                .thenReturn(page);

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarTodasMensalidades(pageable);

            assertNotNull(resultado);

            assertEquals(page.getTotalPages(), resultado.getTotalPages());
            assertEquals(mensalidade.getId(), resultado.getContent().get(0).getId());
            assertEquals(2, resultado.getContent().size());
        }

        @Test   
        void deveLancarExcecaoSemPermissaoParaVerTodasMensalidades() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.buscarTodasMensalidades(pageable)
            );

            assertEquals("Você não tem permissão para visualizar as mensalidades!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(any(Mensalidade.class));
        }

    }

    @Nested
    class buscarMensalidadesPorNomeTest {

        @Test
        void deveBuscarMensalidadesPorNomeComSucesso() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
            mensalidade2.setId(2L);

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Mensalidade> page = new PageImpl<>(mensalidades);

            when(mensalidadeRepository.findByUsuarioNomeContainingIgnoreCase(pageable, usuario.getNome()))
                .thenReturn(page);

            Page<MensalidadeResponseDTO> resultado = mensalidadeService.buscarMensalidadesPorNome(pageable, usuario.getNome());

            assertNotNull(resultado);

            assertEquals(mensalidade.getId(), resultado.getContent().get(0).getId());
            assertEquals(page.getSize(), resultado.getSize());
            assertEquals(2, resultado.getContent().size());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaVisualizarMensalidadesPorNome() {

            when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.buscarMensalidadesPorNome(pageable, usuario.getNome())
            );

            assertEquals("Você não tem permissão para visualizar outras mensalidades!", exception.getMessage());
        }

    }

    @Nested
    class pagarMensalidadeTest {

        @Test
        void devePagarMensalidadeComSucesso() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            MensalidadeResponseDTO resultado = mensalidadeService.pagarMensalidade();

            assertNotNull(resultado);

            assertEquals(mensalidade.getId(), resultado.getId());
            assertEquals(LocalDate.now(), resultado.getDataPagamento());
            assertEquals(StatusMensalidade.PAGA, resultado.getStatus());

            ArgumentCaptor<Mensalidade> captor = ArgumentCaptor.forClass(Mensalidade.class);

            verify(mensalidadeRepository, times(2)).save(captor.capture());

            Mensalidade mensalidadeCapturada = captor.getAllValues().get(0);

            assertEquals(mensalidade.getId(), mensalidadeCapturada.getId());
            assertEquals(StatusMensalidade.PAGA, mensalidadeCapturada.getStatus());
            assertEquals(LocalDate.now(), mensalidadeCapturada.getDataPagamento());
        }

        @Test
        void deveLancarExcecaoMensalidadePagaOuCancelada() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidade.setStatus(StatusMensalidade.CANCELADA);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.pagarMensalidade()
            );

            assertEquals("Apenas mensalidades pendentes(ou atrasadas) podem ser pagas!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(mensalidade);
        }

    }

    @Nested
    class atrasarMensalidadesTest {

        @Test
        void deveAtrasarMensalidadesComSucesso() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidade.setId(1L);
            mensalidade.setDataVencimento(hoje.minusDays(1));

            List<Mensalidade> mensalidades = List.of(mensalidade);

            when(mensalidadeRepository.findByStatusAndDataVencimentoBefore(
                StatusMensalidade.PENDENTE,
                LocalDate.now()))
            .thenReturn(mensalidades);

            mensalidadeService.atrasarMensalidades();

            verify(mensalidadeRepository).findByStatusAndDataVencimentoBefore(
                StatusMensalidade.PENDENTE,
                LocalDate.now()
            );

            verify(mensalidadeRepository).saveAll(mensalidades);

            assertEquals(StatusMensalidade.ATRASADA, mensalidades.get(0).getStatus());
        }

    }

    @Nested
    class excluirMensalidadesAposAnoTest {

        @Test
        void deveExcluirMensalidadesAposUmAnoComSucesso() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidade.setId(1L);
            mensalidade.setDataCriacao(hoje.minusMonths(12));

            List<Mensalidade> mensalidades = List.of(mensalidade);

            when(mensalidadeRepository.findByDataCriacaoBefore(LocalDate.now().minusMonths(12)))
                .thenReturn(mensalidades);

            mensalidadeService.excluirMensalidadesAposAno();

            verify(mensalidadeRepository).delete(mensalidade);
        }

    }

    @Nested
    class cancelarMensalidadeTest {

        @Test
        void deveCancelarMensalidadeComSucesso() {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidade.setId(1L);

            AcessoAcademia acessoAcademia = new AcessoAcademia();
            acessoAcademia.setDiasAcesso(0);
            acessoAcademia.setId(2L);
            acessoAcademia.setInicioSemana(hoje);
            acessoAcademia.setNome(usuario.getNome());
            acessoAcademia.setUltimoAcesso(hoje);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            when(academiaRepository.findByUsuario(usuario))
                .thenReturn(Optional.of(acessoAcademia));

            MensalidadeResponseDTO resultado = mensalidadeService.cancelarMensalidade();

            assertNotNull(resultado);
            
            assertEquals(StatusMensalidade.CANCELADA, resultado.getStatus());
            assertEquals(hoje, resultado.getDataCancelamento());
            assertEquals(mensalidade.getId(), resultado.getId());

            verify(mensalidadeRepository).save(mensalidade);

            verify(academiaRepository).delete(acessoAcademia);
        }

        @Test
        void deveLancarExcecaoApenasMensalidadesPendentesPodemSerCanceladas() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.PAGA);
            mensalidade.setId(1L);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.cancelarMensalidade()
            );

            assertEquals("Apenas mensalidades pendentes podem ser canceladas!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(mensalidade);
            verify(academiaRepository, never()).delete(any(AcessoAcademia.class));
        }

    }

    @Nested
    class excluirMensalidadeTest {

        @Test
        void deveExcluirMensalidadeComSucesso() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);
            mensalidade.setStatus(StatusMensalidade.PAGA);

            AcessoAcademia acessoAcademia = criarAcessoAcademia(usuario);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            when(academiaRepository.findByUsuario(usuario))
                .thenReturn(Optional.of(acessoAcademia));

            mensalidadeService.excluirMensalidade();

            verify(mensalidadeRepository).delete(mensalidade);
            verify(academiaRepository).delete(acessoAcademia);
        }

        @Test
        void deveLancarExcecaoApenasMensalidadesPagasOuCanceladasPodemSerExcluidas() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            AcessoAcademia acessoAcademia = criarAcessoAcademia(usuario);

            when(mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario))
                .thenReturn(Optional.of(mensalidade));

            when(academiaRepository.findByUsuario(usuario))
                .thenReturn(Optional.of(acessoAcademia));

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.excluirMensalidade()
            );

            assertEquals("Apenas mensalidades pagas(ou canceladas) podem ser excluídas!", exception.getMessage());

            verify(mensalidadeRepository, never()).delete(mensalidade);
            verify(academiaRepository, never()).delete(acessoAcademia);
        }

    }

    @Nested
    class gerarProximaMensalidadeTest {

        @Test
        void deveGerarProximaMensalidadeComSucesso() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);
            mensalidade.setStatus(StatusMensalidade.PAGA);
            
            MensalidadeResponseDTO resultado = mensalidadeService.gerarProximaMensalidade(mensalidade);

            assertNotNull(resultado);

            assertEquals(StatusMensalidade.PENDENTE, resultado.getStatus());
            assertEquals(mensalidade.getDiasTreino(), resultado.getDiasTreino());

            ArgumentCaptor<Mensalidade> captor = ArgumentCaptor.forClass(Mensalidade.class);

            verify(mensalidadeRepository).save(captor.capture());

            Mensalidade mensalidadeCapturada = captor.getValue();

            assertEquals(StatusMensalidade.PENDENTE, mensalidadeCapturada.getStatus());
            assertEquals(mensalidade.getUsuario(), mensalidadeCapturada.getUsuario());
            assertEquals(mensalidade.getValor(), mensalidadeCapturada.getValor());
        }

        @Test
        void deveLancarExcecaoMensalidadeAindaNaoPaga() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setId(1L);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> mensalidadeService.gerarProximaMensalidade(mensalidade)
            );

            assertEquals("Mensalidade ainda não paga!", exception.getMessage());

            verify(mensalidadeRepository, never()).save(any(Mensalidade.class));
        }

    }

}
