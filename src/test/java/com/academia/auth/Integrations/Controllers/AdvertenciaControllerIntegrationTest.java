package com.academia.auth.Integrations.Controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AdvertenciaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@Transactional 
public class AdvertenciaControllerIntegrationTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private AdvertenciaRepository advertenciaRepository;

    private Usuario criarUsuario() {

        Usuario usuario = Usuario.builder()
                .nome("Saulo")
                .email("saulo@gmail.com")
                .senha("091812")
                .role(RoleUser.ROLE_USER)
                .build();

        return usuario;
    }

    private Advertencia criarAdvertencia(Usuario remetente, Usuario destinatario) {
        
        LocalDateTime agora = LocalDateTime.now();

        return Advertencia.builder()
            .mensagem("Equipamento quebrado!")
            .nivelAdvertencia(AdvertenciaStatus.LEVE)
            .dataCriacao(agora)
            .dataExpiracao(agora.plusDays(3))
            .destinatario(destinatario)
            .remetente(remetente)
        .build();
    }

    @Nested 
    class EnviarAdvertenciaTest {

        private Usuario usuario;
        private Usuario aluno;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);
            usuarioRepository.save(usuario);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");
            usuarioRepository.save(aluno);
        }

        @Test 
        void deveEnviarAdvertenciaComSucesso() throws Exception {

            mockMvc.perform(
                post("/advertencia/" + aluno.getId() + "/enviar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "mensagem": "Equipamento quebrado!",
                        "nivel": "LEVE"
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nivelAdvertencia").value("LEVE"))
            .andExpect(jsonPath("$.mensagem").value("Equipamento quebrado!"))
            .andExpect(jsonPath("$.destinatario").value(aluno.getNome()));
        }

        @Test 
        void deveImpedirEnviarAdvertenciaParaFuncionarios() throws Exception {

            aluno.setRole(RoleUser.ROLE_FUNCIONARIO);

            mockMvc.perform(
                post("/advertencia/" + aluno.getId() + "/enviar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "mensagem": "Equipamento quebrado!",
                        "nivel": "LEVE"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
        }

        @Test 
        void deveImpedirEnviarAdvertenciaAlunoComMaisDeTres() throws Exception {

            var advertencia = criarAdvertencia(usuario, aluno);
            var advertencia2 = criarAdvertencia(usuario, aluno);
            var advertencia3 = criarAdvertencia(usuario, aluno);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2, advertencia3);

            advertenciaRepository.saveAll(advertencias);

            mockMvc.perform(
                post("/advertencia/" + aluno.getId() + "/enviar")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "mensagem": "Equipamento quebrado!",
                        "nivel": "LEVE"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
        }

    }

    @Nested 
    class MostrarSuasAdvertenciasEnviadasTest {

        private Usuario usuario;
        private Usuario aluno;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            List<Usuario> usuarios = List.of(usuario, aluno);

            usuarioRepository.saveAll(usuarios);
        }

        @Test 
        void deveBuscarSuasAdvertenciasEnviadas() throws Exception {

            var advertencia = criarAdvertencia(usuario, aluno);
            var advertencia2 = criarAdvertencia(usuario, aluno);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            advertenciaRepository.saveAll(advertencias);

            mockMvc.perform(
                get("/advertencia/enviadas/me")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                advertencia.getId().intValue(), 
                advertencia2.getId().intValue()))
            );
        }
        
        @Test 
        void deveImpedirUsuarioSemPermissao() throws Exception {

            mockMvc.perform(
                get("/advertencia/enviadas/me")
                .with(user(aluno))
            )
            .andExpect(status().isForbidden());
        }

    }

    @Nested 
    class MostrarSuasAdvertenciasRecebidasTest {

        private Usuario usuario;
        private Usuario aluno;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            List<Usuario> usuarios = List.of(usuario, aluno);

            usuarioRepository.saveAll(usuarios);
        }

        @Test 
        void deveBuscarSuasMensalidadesRecebidas() throws Exception {

            var advertencia = criarAdvertencia(usuario, aluno);
            var advertencia2 = criarAdvertencia(usuario, aluno);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            advertenciaRepository.saveAll(advertencias);

            mockMvc.perform(
                get("/advertencia/recebidas/me")
                .with(user(aluno))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                advertencia.getId().intValue(), 
                advertencia2.getId().intValue()))
            );
        }

    }

    @Nested 
    class BuscarTodasAdvertenciasPorFiltroTest {

        private Usuario usuario;
        private Usuario aluno;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            List<Usuario> usuarios = List.of(usuario, aluno);

            usuarioRepository.saveAll(usuarios);
        }

        @Test
        void deveBuscarTodasAdvertenciasPorFiltroComSucesso() throws Exception {

            var advertencia = criarAdvertencia(usuario, aluno);

            var advertencia2 = criarAdvertencia(usuario, aluno);
            advertencia2.setNivelAdvertencia(AdvertenciaStatus.GRAVE);

            List<Advertencia> advertencias = List.of(advertencia, advertencia2);

            advertenciaRepository.saveAll(advertencias);

            mockMvc.perform(
                get("/advertencia/pesquisar")
                .with(user(usuario))
                .param("nivelAdvertencia", "LEVE")
                .param("destinatario", "sau")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].nivelAdvertencia").value("LEVE"))
            .andExpect(jsonPath("$.content[0].destinatario").value("Saulo"));
        }

        @Test 
        void deveImpedirUsuarioSemPermissaoBuscarAdvertenciasComFiltro() throws Exception {

            mockMvc.perform(
                get("/advertencia/pesquisar")
                .with(user(aluno))
            )
            .andExpect(status().isForbidden());
        }

    }

    @Nested 
    class BuscarAdvertenciaPorIdTest {

        private Usuario usuario;
        private Usuario aluno;
        private Advertencia advertencia;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            List<Usuario> usuarios = List.of(usuario, aluno);
            
            usuarioRepository.saveAll(usuarios);
            
            advertencia = criarAdvertencia(usuario, aluno);
            
            advertenciaRepository.save(advertencia);
        }

        @Test 
        void deveBuscarAdvertenciaPorIdComSucesso() throws Exception {

            mockMvc.perform(
                get("/advertencia/" + advertencia.getId() + "/buscar")
                .with(user(usuario))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(advertencia.getId()))
            .andExpect(jsonPath("$.remetente").value(usuario.getNome()));
        }

        @Test 
        void deveImpedirUsuarioSemPermissaoBuscarAdvertenciaPorId() throws Exception {

            mockMvc.perform(
                get("/advertencia/" + advertencia.getId() + "/buscar")
                .with(user(aluno))
            )
            .andExpect(status().isForbidden());
        }

    }

    @Nested 
    class ExcluirAdvertenciaTest {

        private Usuario usuario;
        private Usuario aluno;
        private Advertencia advertencia;

        @BeforeEach     
        void prepararSetup() {

            usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            aluno = criarUsuario();
            aluno.setEmail("aluno@gmail.com");

            List<Usuario> usuarios = List.of(usuario, aluno);

            usuarioRepository.saveAll(usuarios);

            advertencia = criarAdvertencia(usuario, aluno);
            advertenciaRepository.save(advertencia);
        }
    
        @Test 
        void deveExcluirAdvertenciaComSucesso() throws Exception {

            mockMvc.perform(
                delete("/advertencia/" + advertencia.getId() + "/deletar")
                .with(user(usuario))
            )
            .andExpect(status().isNoContent());

            Optional<Advertencia> advertenciaExcluida = advertenciaRepository.findById(advertencia.getId());

            assertThat(advertenciaExcluida).isEmpty();
        }
 
        @Test   
        void deveImpedirUsuarioSemPermissaoExcluirAdvertencia() throws Exception {

            mockMvc.perform(
                delete("/advertencia/" + advertencia.getId() + "/deletar")
                .with(user(aluno))
            )
            .andExpect(status().isForbidden());
        }

        @Test 
        void devePermitirAdminExcluirQualquerAdvertencia() throws Exception {

            usuario .setRole(RoleUser.ROLE_ADMIN);

            mockMvc.perform(
                delete("/advertencia/" + advertencia.getId() + "/deletar")
                .with(user(usuario))
            )
            .andExpect(status().isNoContent());
        }

    }

}

