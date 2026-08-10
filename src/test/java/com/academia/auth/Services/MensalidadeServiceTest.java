package com.academia.auth.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    
        when(usuarioLogado.usuarioLogado())
            .thenReturn(usuario);
    }

    @Nested
    class criarMensalidadeTest {

        @Test
        void deveCriarMensalidadeComSucesso() {

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

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = new Mensalidade();

            mensalidade.setDataCriacao(LocalDate.now());
            mensalidade.setDataVencimento(LocalDate.now().plusMonths(1));
            mensalidade.setUsuario(usuario);
            mensalidade.setDiasTreino(dto.getDiasTreino());
            mensalidade.setDataPagamento(null);
            mensalidade.setDataCancelamento(null);
            mensalidade.setAtualizacoes(0);
            mensalidade.setStatus(StatusMensalidade.PENDENTE);

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

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = new Mensalidade();

            mensalidade.setDataCriacao(LocalDate.now());
            mensalidade.setDataVencimento(LocalDate.now().plusMonths(1));
            mensalidade.setUsuario(usuario);
            mensalidade.setDiasTreino(dto.getDiasTreino());
            mensalidade.setDataPagamento(LocalDate.now());
            mensalidade.setDataCancelamento(null);
            mensalidade.setAtualizacoes(0);
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

            MensalidadeRequestDTO dto = new MensalidadeRequestDTO(3);

            Mensalidade mensalidade = new Mensalidade();

            mensalidade.setDataCriacao(LocalDate.now());
            mensalidade.setDataVencimento(LocalDate.now().plusMonths(1));
            mensalidade.setUsuario(usuario);
            mensalidade.setDiasTreino(dto.getDiasTreino());
            mensalidade.setDataPagamento(LocalDate.now());
            mensalidade.setDataCancelamento(null);
            mensalidade.setAtualizacoes(1);
            mensalidade.setStatus(StatusMensalidade.PENDENTE);

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

            Mensalidade mensalidade = new Mensalidade();

            mensalidade.setValor(BigDecimal.valueOf(50));
            mensalidade.setDataCriacao(LocalDate.now());
            mensalidade.setDataVencimento(LocalDate.now().plusMonths(1));
            mensalidade.setUsuario(usuario);
            mensalidade.setDiasTreino(3);
            mensalidade.setDataPagamento(null);
            mensalidade.setDataCancelamento(null);
            mensalidade.setAtualizacoes(0);
            mensalidade.setStatus(StatusMensalidade.PENDENTE);

            Mensalidade mensalidade2 = new Mensalidade();

            mensalidade2.setValor(BigDecimal.valueOf(50));
            mensalidade2.setDataCriacao(LocalDate.now());
            mensalidade2.setDataVencimento(LocalDate.now().plusMonths(1));
            mensalidade2.setUsuario(usuario);
            mensalidade2.setDiasTreino(3);
            mensalidade2.setDataPagamento(null);
            mensalidade2.setDataCancelamento(null);
            mensalidade2.setAtualizacoes(0);
            mensalidade2.setStatus(StatusMensalidade.PENDENTE);

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

        
    }
}
