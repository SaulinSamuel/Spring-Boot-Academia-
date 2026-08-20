package com.academia.auth.Integrations.Controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UsuarioControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AcessoAcademiaRepository acessoAcademiaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //helpers
    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setNome("Saulo");
        usuario.setEmail("saulo@gmail.com");
        usuario.setRole(RoleUser.ROLE_USER);
        usuario.setSenha(passwordEncoder.encode("091812"));
        
        return usuario;
    }

    //tests
    @Nested
    class atualizarUsuarioTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            usuarioRepository.save(usuario);
        }

        @Test
        void deveAtualizarUsuarioComSucesso() throws Exception {

            mockMvc.perform(
                put("/usuario/atualizar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "Saulin",
                            "email": "sauloteste@gmail.com",
                            "senhaAtual": "091812"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Saulin"))
            .andExpect(jsonPath("$.email").value("sauloteste@gmail.com"));
        }

        @Test
        void deveAtualizarUsuarioComSenhaNovaComSucesso() throws Exception {

            mockMvc.perform(
                put("/usuario/atualizar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "Saulin",
                            "email": "sauloteste@gmail.com",
                            "senhaAtual": "091812",
                            "senhaNova": "123456"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Saulin"))
            .andExpect(jsonPath("$.email").value("sauloteste@gmail.com"));
        }
       
        @Test
        void deveImpedirAtualizarUsuarioSenhaIncorreta() throws Exception {

            mockMvc.perform(
                put("/usuario/atualizar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "Saulin",
                            "email": "sauloteste@gmail.com",
                            "senhaAtual": "091813"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
        }

        @Test
        void deveImpedirAtualizarUsuarioEmailJaExistente() throws Exception {

            Usuario usuarioExistente = criarUsuario();
            usuarioExistente.setEmail("existe@gmail.com");

            usuarioRepository.save(usuarioExistente);

            mockMvc.perform(
                put("/usuario/atualizar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "Saulin",
                            "email": "existe@gmail.com",
                            "senhaAtual": "091812"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
        }   
        
    }

    @Nested
    class listarUsuariosTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);
        }

        @Test
        void deveListarUsuariosComSucesso() throws Exception {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = criarUsuario();
            usuario2.setEmail("teste1@gmail.com");

            Usuario usuario3 = criarUsuario();
            usuario3.setEmail("teste3@gmail.com");

            List<Usuario> usuarios = List.of(usuario2, usuario3);
            usuarioRepository.saveAll(usuarios);

            mockMvc.perform(
                get("/usuario/listar")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                usuario.getId().intValue(), 
                usuario2.getId().intValue(), 
                usuario3.getId().intValue())));
        }

        @Test
        void deveImpedirUsuariosComunsDeListarTodosUsuarios() throws Exception {

            mockMvc.perform(
                get("/usuario/listar")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class listarUsuariosPorNomeTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();

            usuarioRepository.save(usuario);
        }   

        @Test
        void deveListarUsuariosPorNomeComSucesso() throws Exception {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = criarUsuario();
            usuario2.setEmail("teste@gmail.com");

            Usuario usuario3 = criarUsuario();
            usuario3.setEmail("teste1@gmil.com");

            List<Usuario> usuarios = List.of(usuario2, usuario3);

            usuarioRepository.saveAll(usuarios);

            mockMvc.perform(
                get("/usuario/pesquisar")
                .with(user(usuario))
                .param("nome", "sau")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                usuario.getId().intValue(),
                usuario2.getId().intValue(),
                usuario3.getId().intValue()
            )));
        }
        
        @Test
        void deveImpedirUsuarioComumListarUsuariosPorNome() throws Exception {

            mockMvc.perform(
                get("/usuario/pesquisar")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }
    
    }   

    @Nested
    class meusDadosTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            usuarioRepository.save(usuario);
        }

        @Test
        void deveRetornarDadosDoUsuarioLogado() throws Exception {

            mockMvc.perform(
                get("/usuario/me")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.getId().intValue()));
        }

    }

    @Nested
    class promoverUsuarioAFuncionarioTest {

        private Usuario usuario;
        private Usuario usuarioPromovido;

        @BeforeEach
        void prepararSetup() {
            
            usuario = criarUsuario();
            usuarioRepository.save(usuario);

            usuarioPromovido = criarUsuario();
            usuarioPromovido.setEmail("teste@gmail.com");

            usuarioRepository.save(usuarioPromovido);
        }

        @Test
        void devePromoverUsuarioAFuncionario() throws Exception {

            usuario.setRole(RoleUser.ROLE_ADMIN);

            mockMvc.perform(
                patch("/usuario/" + usuarioPromovido.getId() + "/promover-funcionario")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ROLE_FUNCIONARIO"));

            Optional<AcessoAcademia> acessoAcademia = acessoAcademiaRepository.findByUsuario(usuarioPromovido);
            
            assertThat(acessoAcademia).isNotEmpty();
        }

        @Test
        void deveImpedirUsuarioNaoAdminPromovaUsuario() throws Exception {

            mockMvc.perform(
                patch("/usuario/" + usuarioPromovido.getId() + "/promover-funcionario")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }

        @Test
        void deveImpedirPromoverUsuarioNaoUser() throws Exception {

            usuario.setRole(RoleUser.ROLE_ADMIN);
            usuarioPromovido.setRole(RoleUser.ROLE_FUNCIONARIO);
            
            mockMvc.perform(
                patch("/usuario/" + usuarioPromovido.getId() + "/promover-funcionario")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class rebaixarFuncionarioAUsuarioTest {

        private Usuario usuario;

        private Usuario usuarioRebaixado;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
        
            usuarioRebaixado = criarUsuario();
            usuario.setEmail("teste@gmail.com");
            usuarioRebaixado.setRole(RoleUser.ROLE_FUNCIONARIO);

            List<Usuario> usuarios = List.of(usuario, usuarioRebaixado);

            usuarioRepository.saveAll(usuarios);
        }

        @Test
        void deveRebaixarFuncionarioAUsuarioComSucesso() throws Exception {

            usuario.setRole(RoleUser.ROLE_ADMIN);

            mockMvc.perform(
                patch("/usuario/" + usuarioRebaixado.getId() + "/rebaixar-usuario")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ROLE_USER"));
        }

        @Test
        void deveImpedirUsuarioNaoAdminOuSemPermissaoDeRebaixarFuncionario() throws Exception {

            mockMvc.perform(
                patch("/usuario/" + usuarioRebaixado.getId() + "/rebaixar-usuario")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }

        @Test
        void deveImpedirQueUsuarioNaoFuncionarioSejaRebaixado() throws Exception {

            usuario.setRole(RoleUser.ROLE_ADMIN);
            usuarioRebaixado.setRole(RoleUser.ROLE_USER);

            mockMvc.perform(
                patch("/usuario/" + usuarioRebaixado.getId() + "/rebaixar-usuario")
                .with(user(usuario))
            )
            .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class deletarUsuarioTest {

        private Usuario usuario;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            usuarioRepository.save(usuario);
        }
        
        @Test
        void deveExcluirContaComSucesso() throws Exception {

            mockMvc.perform(
                delete("/usuario")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "senhaAtual": "091812"
                        }
                        """)
            )
            .andExpect(status().isNoContent());
        }

        @Test
        void deveImpedirExclusaoDeContaSenhaIncorreta() throws Exception {

            mockMvc.perform(
                delete("/usuario")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "senhaAtual": "091831"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
        }

    }

}   
