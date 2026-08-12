package com.academia.auth.Integrations.Repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class MensalidadeRepositoryTest {

    @Autowired
    private MensalidadeRepository mensalidadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

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

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);

        return usuario;
    }

    @Test
    void deveRetornarMensalidadeUltimaMensalidadePorUsuario() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setDataCriacao(LocalDate.now().minusDays(1));

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
        mensalidade2.setStatus(StatusMensalidade.PAGA);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        Optional<Mensalidade> resultado = mensalidadeRepository.findTopByUsuarioOrderByDataCriacaoDesc(usuario);

        assertThat(resultado).isPresent();

        assertEquals(mensalidade2.getDataCriacao(), resultado.get().getDataCriacao());
        assertEquals(mensalidade2.getId(), resultado.get().getId());
    }

    @Test
    void deveRetornarVazioSeUltimaMensalidadePorUsuarioNaoExistir() {

        Usuario usuario = criarUsuario();

        usuarioRepository.save(usuario);

        Optional<Mensalidade> resultado = mensalidadeRepository.findTopByUsuarioOrderByDataCriacaoDesc(usuario);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveContarMensalidadesPeloStatus() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setStatus(StatusMensalidade.ATRASADA);

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        Long quantidadeMensalidadesPorStatus = mensalidadeRepository.countByStatus(StatusMensalidade.ATRASADA);

        assertNotNull(quantidadeMensalidadesPorStatus);

        assertEquals(1L, quantidadeMensalidadesPorStatus);
    }

    @Test
    void deveSomarPorPeriodoEStatus() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setDataPagamento(LocalDate.now());

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);
        mensalidade2.setDataPagamento(LocalDate.now());

        Mensalidade mensalidade3 = criarMensalidadePendente(usuario);
        mensalidade3.setDataPagamento(LocalDate.now());

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2, mensalidade3);

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = inicio.withDayOfMonth(hoje.lengthOfMonth());

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        entityManager.flush();
        entityManager.clear();

        BigDecimal valorTotalSomados = mensalidadeRepository.somarValorPorPeriodo(
            StatusMensalidade.PENDENTE, 
            inicio,
            fim
        );

        assertNotNull(valorTotalSomados);

        assertEquals(0, BigDecimal.valueOf(150).compareTo(valorTotalSomados));
    }

    @Test
    void deveRetornarTodasMensalidadesPorPaginacao() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

        Pageable pageable = PageRequest.of(0, 10);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        Page<Mensalidade> resultado = mensalidadeRepository.findAllByUsuario(usuario, pageable);

        assertNotNull(resultado);
        
        assertEquals(2, resultado.getContent().size());
        assertThat(resultado.getContent()).extracting(Mensalidade::getId)
            .containsExactlyInAnyOrder(mensalidade.getId(), mensalidade2.getId());
    }

    @Test
    void deveRetornarMensalidadesPaginadasPorNomeDoUsuarioContendoIgnoringCase() {

        Usuario usuario = criarUsuario();
    
        Mensalidade mensalidade = criarMensalidadePendente(usuario);

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

        Pageable pageable = PageRequest.of(0, 10);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        Page<Mensalidade> resultado = mensalidadeRepository.findByUsuarioNomeContainingIgnoreCase(
            pageable, 
            "Sau"
        );

        assertNotNull(resultado);

        assertEquals(2, resultado.getContent().size());
        assertThat(resultado.getContent()).extracting(Mensalidade::getId)
            .containsExactlyInAnyOrder(mensalidade.getId(), mensalidade2.getId());
    }

    @Test
    void deveRetornarTrueQuandoExistirMensalidadePorUsuario() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);

        usuarioRepository.save(usuario);
        mensalidadeRepository.save(mensalidade);

        boolean existePorUsuario = mensalidadeRepository.existsByUsuario(usuario);

        assertTrue(existePorUsuario);
    }

    @Test
    void deveRetornarFalseQuandoNaoExistirMensalidadePorUsuario() {

        Usuario usuario = criarUsuario();

        usuarioRepository.save(usuario);

        boolean existePorUsuario = mensalidadeRepository.existsByUsuario(usuario);

        assertFalse(existePorUsuario);
    }

    @Test
    void deveRetornarTrueQuandoExistirMensalidadePorUsuarioEDataCancelamentoPorPeriodo() {

        Usuario usuario = criarUsuario();

        LocalDate hoje = LocalDate.now();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setDataCancelamento(hoje);

        usuarioRepository.save(usuario);

        mensalidadeRepository.save(mensalidade);

        boolean existePorPeriodo = mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween(
            usuario, 
            hoje,
            hoje
        );

        assertTrue(existePorPeriodo);
    }

    @Test
    void deveRetornarFalseQuandoNaoExistirMensalidadePorUsuarioEDataCancelamentoPorPeriodo() {

        Usuario usuario = criarUsuario();

        LocalDate hoje = LocalDate.now();

        usuarioRepository.save(usuario);

        boolean existePorPeriodo = mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween(usuario, 
            hoje,
            hoje
        );

        assertThat(existePorPeriodo).isFalse();
    }

    @Test
    void deveRetornarTrueQuandoExistirMensalidadePorUsuarioEStatus() {

        Usuario usuario = criarUsuario();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);

        usuarioRepository.save(usuario);

        mensalidadeRepository.save(mensalidade);

        boolean existePorUsuarioEStatus = mensalidadeRepository.existsByUsuarioAndStatus(
            usuario,
            StatusMensalidade.PENDENTE
        );

        assertThat(existePorUsuarioEStatus).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoNaoExistirMensalidadePorUsuarioEStatus() {

        Usuario usuario = criarUsuario();

        usuarioRepository.save(usuario);

        boolean existePorUsuarioEStatus = mensalidadeRepository.existsByUsuarioAndStatus(
            usuario, 
            StatusMensalidade.PENDENTE
        );

        assertThat(existePorUsuarioEStatus).isFalse();
    }

    @Test
    void deveRetornarMensalidadesPorStatusEDataVencimentoAntes() {

        Usuario usuario = criarUsuario();

        LocalDate hoje = LocalDate.now();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setDataVencimento(hoje.minusDays(1));

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        List<Mensalidade> resultado = mensalidadeRepository.findByStatusAndDataVencimentoBefore(
            StatusMensalidade.PENDENTE,
            hoje
        );

        assertNotNull(resultado);

        assertThat(resultado).extracting(Mensalidade::getId)
            .containsExactlyInAnyOrder(mensalidade.getId());
    }

    @Test
    void deveRetornarMensalidadesPorDataDeCriacaoAntes() {

        Usuario usuario = criarUsuario();

        LocalDate hoje = LocalDate.now();

        Mensalidade mensalidade = criarMensalidadePendente(usuario);
        mensalidade.setDataCriacao(hoje.minusDays(1));

        Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

        List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

        usuarioRepository.save(usuario);

        mensalidadeRepository.saveAll(mensalidades);

        List<Mensalidade> resultado = mensalidadeRepository.findByDataCriacaoBefore(hoje);

        assertNotNull(resultado);

        assertThat(resultado).extracting(Mensalidade::getId)
            .containsExactlyInAnyOrder(mensalidade.getId());
    }

}
