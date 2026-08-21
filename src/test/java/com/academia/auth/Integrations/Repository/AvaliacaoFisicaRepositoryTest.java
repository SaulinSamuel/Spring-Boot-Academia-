package com.academia.auth.Integrations.Repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.academia.auth.Models.AvaliacaoFisica;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AvaliacaoFisicaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;

@DataJpaTest
@Import(TestContainersConfig.class)
public class AvaliacaoFisicaRepositoryTest {

    @Autowired
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);

        return usuario;
    }

    @Test
    void deveRetornarTrueSeExisteAvaliacaoFisicaPorAlunoEDataAvaliacaoPorPeriodo() {

        Usuario aluno = criarUsuario();
        aluno.setEmail("aluno@gmail.com");

        Usuario avaliador = criarUsuario();
        avaliador.setRole(RoleUser.ROLE_FUNCIONARIO);

        List<Usuario> usuarios = List.of(aluno, avaliador);

        usuarioRepository.saveAll(usuarios);

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());

        AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
        avaliacaoFisica.setAvaliador(avaliador);
        avaliacaoFisica.setDataAvaliacao(hoje);

        avaliacaoFisicaRepository.save(avaliacaoFisica);

        boolean existeAvaliacaoPorAlunoEDataAvaliacao = avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
            aluno, 
            inicio, 
            fim
        );

        assertThat(existeAvaliacaoPorAlunoEDataAvaliacao).isTrue();
    }

    @Test
    void deveRetornarFalseSeNaoExisteAvaliacaoFisicaPorAlunoEDataAvaliacaoPorPeriodo() {

        Usuario aluno = criarUsuario();
        aluno.setEmail("aluno@gmail.com");

        Usuario aluno2 = criarUsuario();

        Usuario avaliador = criarUsuario();
        avaliador.setEmail("avaliador@gmail.com");

        List<Usuario> usuarios = List.of(aluno, aluno2, avaliador);

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());

        usuarioRepository.saveAll(usuarios);

        AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
        avaliacaoFisica.setDataAvaliacao(hoje);
        avaliacaoFisica.setAvaliador(avaliador);

        avaliacaoFisicaRepository.save(avaliacaoFisica);

        boolean existePorUsuarioEDataAvaliacao = avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
            aluno2, 
            inicio, 
            fim
        );

        assertThat(existePorUsuarioEDataAvaliacao).isFalse();
    }
    
    @Test   
    void deveRetornarTodasAvaliacoesFisicasPageadasPorAvaliador() {

        Usuario aluno = criarUsuario();

        Usuario avaliador = criarUsuario();
        avaliador.setRole(RoleUser.ROLE_FUNCIONARIO);
        avaliador.setEmail("avaliador@gmail.com");

        List<Usuario> usuarios = List.of(aluno, avaliador);

        Pageable pageable = PageRequest.of(0, 10);

        LocalDate hoje = LocalDate.now();

        AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
        avaliacaoFisica.setAvaliador(avaliador);
        avaliacaoFisica.setDataAvaliacao(hoje);

        AvaliacaoFisica avaliacaoFisica2 = criarAvaliacaoFisica(aluno);
        avaliacaoFisica2.setAvaliador(avaliador);
        avaliacaoFisica2.setDataAvaliacao(hoje);

        List<AvaliacaoFisica> avaliacaoFisicas = List.of(avaliacaoFisica, avaliacaoFisica2);

        usuarioRepository.saveAll(usuarios);
        avaliacaoFisicaRepository.saveAll(avaliacaoFisicas);

        Page<AvaliacaoFisica> page = avaliacaoFisicaRepository.findAllByAvaliador(
            avaliador, 
            pageable
        );

        assertThat(page).isNotEmpty();

        assertThat(page).extracting(AvaliacaoFisica::getId)
            .containsExactlyInAnyOrder(avaliacaoFisica.getId(), avaliacaoFisica2.getId());
    }

    @Test
    void deveRetornarTodasAvaliacoesFisicasPageadasPorAluno() {

        Usuario aluno = criarUsuario();

        Usuario avaliador = criarUsuario();
        avaliador.setEmail("avaliador@gmail.com");
        avaliador.setRole(RoleUser.ROLE_FUNCIONARIO);

        Pageable pageable = PageRequest.of(0, 10);

        LocalDate hoje = LocalDate.now();

        AvaliacaoFisica avaliacaoFisica = criarAvaliacaoFisica(aluno);
        avaliacaoFisica.setAvaliador(avaliador);
        avaliacaoFisica.setDataAvaliacao(hoje);

        AvaliacaoFisica avaliacaoFisica2 = criarAvaliacaoFisica(aluno);
        avaliacaoFisica2.setAvaliador(avaliador);
        avaliacaoFisica2.setDataAvaliacao(hoje);

        List<Usuario> usuarios = List.of(aluno, avaliador);
        List<AvaliacaoFisica> avaliacaoFisicas = List.of(avaliacaoFisica, avaliacaoFisica2);
        
        usuarioRepository.saveAll(usuarios);
        avaliacaoFisicaRepository.saveAll(avaliacaoFisicas);

        Page<AvaliacaoFisica> page = avaliacaoFisicaRepository.findAllByAluno(aluno, pageable);

        assertThat(page).isNotEmpty();

        assertThat(page).extracting(AvaliacaoFisica::getId)
            .containsExactlyInAnyOrder(avaliacaoFisica.getId(), avaliacaoFisica2.getId());
    }

}
