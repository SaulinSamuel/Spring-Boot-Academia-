package com.academia.auth.Integrations.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.UsuarioRepository;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setRole(RoleUser.ROLE_USER);
        usuario.setSenha("091812");

        return usuario;
    }

    @Test
    void deveEncontrarUsuarioPeloEmail() {

        Usuario usuario = criarUsuario();

        usuarioRepository.save(usuario);

        Optional<Usuario> resultado = usuarioRepository.findByEmail(usuario.getEmail());

        assertEquals(usuario.getId(), resultado.get().getId());
        assertEquals(usuario.getEmail(), resultado.get().getEmail());
    }

    @Test
    void deveRetornarVazioSeEmailNaoExistir() {

        Optional<Usuario> resultado = usuarioRepository.findByEmail("saulo@gmail.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarTrueQuandoExistir() {

        Usuario usuario = criarUsuario();

        usuarioRepository.save(usuario);

        boolean emailExiste = usuarioRepository.existsByEmail(usuario.getEmail());

        assertTrue(emailExiste);
    }

    @Test
    void deveRetornarFalseQuandoNaoExistir() {

        boolean emailExiste = usuarioRepository.existsByEmail("teste@gmail.com");

        assertFalse(emailExiste);
    }

    @Test
    void deveContarUsuariosPelaRole() {

        Usuario usuario = criarUsuario();
        Usuario usuario2 = criarUsuario();
        usuario2.setEmail("teste2@gmail.com");

        Usuario usuario3 = criarUsuario();
        usuario3.setEmail("teste1@gmail.com");
        usuario3.setRole(RoleUser.ROLE_FUNCIONARIO);

        List<Usuario> usuarios = List.of(usuario, usuario2, usuario3);

        usuarioRepository.saveAll(usuarios);

        long quantidadeDeUsers = usuarioRepository.countByRole(RoleUser.ROLE_USER);

        assertEquals(2, quantidadeDeUsers);
    }

    @Test
    void deveRetornarQuandoExistirUsuarioComMesmoEmail() {

        Usuario usuario = criarUsuario();

        Usuario usuario2 = criarUsuario();
        usuario2.setEmail("teste@gmail.com");

        List<Usuario> usuarios = List.of(usuario, usuario2);

        usuarioRepository.saveAll(usuarios);

        boolean existePorEmailEIdNao = usuarioRepository.existsByEmailAndIdNot(
            "teste@gmail.com", 
            usuario.getId()
        );

        assertTrue(existePorEmailEIdNao);
    }

    @Test
    void deveRetornarFalseQuandoNaoExistirUsuarioComMesmoEmail() {

        Usuario usuario = criarUsuario();

        Usuario usuario2 = criarUsuario();
        usuario2.setEmail("saulo2@gmail.com");

        List<Usuario> usuarios = List.of(usuario, usuario2);

        usuarioRepository.saveAll(usuarios);

        boolean existePorEmailEIdNao = usuarioRepository.existsByEmailAndIdNot(
            "saulin@gmail.com",
            usuario.getId()
        );

        assertFalse(existePorEmailEIdNao);
    }

}