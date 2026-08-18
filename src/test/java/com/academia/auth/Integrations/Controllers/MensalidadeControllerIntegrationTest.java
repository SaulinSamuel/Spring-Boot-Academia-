package com.academia.auth.Integrations.Controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MensalidadeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensalidadeRepository mensalidadeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario criarUsuario() {

        Usuario usuario = Usuario.builder()
                .nome("Saulo")
                .email("saulo@gmail.com")
                .senha(passwordEncoder.encode("091812"))
                .role(RoleUser.ROLE_USER)
                .build();

        return usuarioRepository.save(usuario);
    }

    private Usuario criarUsuarioSemSalvar() {

        Usuario usuario = Usuario.builder()
                .nome("Saulo")
                .email("saulo@gmail.com")
                .senha(passwordEncoder.encode("091812"))
                .role(RoleUser.ROLE_USER)
                .build();

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
 
    private String gerarToken(Usuario usuario) {
        return jwtService.gerarToken(usuario);
    }   

    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {

        mockMvc.perform(
            post("/mensalidade")
        )
        .andDo(print())
        .andExpect(status().isUnauthorized());
    }

    @Nested
    class criarMensalidadeTest {
        
        private Usuario usuario;
        private String token;
        
        @BeforeEach
        void prepararUsuarioComToken() {

            usuario = criarUsuario();
            token = gerarToken(usuario);
        }

        @Test
        void deveCriarMensalidadePendente() throws Exception {

            mockMvc.perform(
                post("/mensalidade")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "diasTreino": 3
                            }
                            """)
            )
            .andExpect(status().isCreated());

            List<Mensalidade> mensalidades = mensalidadeRepository.findAll();

            assertThat(mensalidades).hasSize(1);

            Mensalidade mensalidade = mensalidades.get(0);

            assertThat(mensalidade.getUsuario().getEmail()).isEqualTo(usuario.getEmail());
            assertThat(mensalidade.getStatus()).isEqualTo(StatusMensalidade.PENDENTE);
            assertThat(mensalidade.getDiasTreino()).isEqualTo(3);
        }

        @Test
        void deveImpedirCriarMensalidadeComMensalidadeJaPendente() throws Exception {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidadeRepository.save(mensalidade);

            mockMvc.perform(
                post("/mensalidade")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "diasTreino": 3
                        }
                        """)
            )
            .andExpect(status().isBadRequest());

            Optional<Mensalidade> mensalidadePendente = mensalidadeRepository.findById(mensalidade.getId());
            
            assertThat(mensalidadePendente.get().getStatus()).isEqualTo(mensalidade.getStatus());
        }

        @Test
        void deveImpedirCriarMensalidadeSeJaCancelouUmaNoMes() throws Exception {

            LocalDate hoje = LocalDate.now();

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setDataCancelamento(hoje);
            mensalidade.setStatus(StatusMensalidade.CANCELADA);

            mensalidadeRepository.save(mensalidade);

            mockMvc.perform(
                post("/mensalidade")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "diasTreino": 3
                        }
                        """)
            )
            .andExpect(status().isBadRequest());

            Optional<Mensalidade> mensalidadePendente = mensalidadeRepository.findById(mensalidade.getId());
            
            assertThat(mensalidadePendente.get().getStatus()).isEqualTo(mensalidade.getStatus());
        }
 
    }

    @Nested
    class atualizarMensalidadeTest {

        private Usuario usuario;
        private String token;
        
        @BeforeEach
        void prepararUsuarioComToken() {

            usuario = criarUsuario();
            token = gerarToken(usuario);
        }

        @Test
        void deveAtualizarMensalidadeComSucesso() throws Exception {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);

            mockMvc.perform(
                put("/mensalidade/atualizar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "diasTreino": 2
                        }
                        """)
            )
            .andExpect(status().isOk());

            Optional<Mensalidade> mensalidadeAtualizada = mensalidadeRepository.findById(mensalidade.getId());

            assertThat(mensalidadeAtualizada.get().getAtualizacoes()).isEqualTo(1);
            assertThat(mensalidadeAtualizada.get().getDiasTreino()).isEqualTo(2);
        }

        @Test
        void deveImpedirMensalidadeNaoPendenteAoAtualizar() throws Exception {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setStatus(StatusMensalidade.ATRASADA);

            mensalidadeRepository.save(mensalidade);

            mockMvc.perform(
                put("/mensalidade/atualizar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "diasTreino": 2
                        }
                        """)
            )
            .andExpect(status().isBadRequest());

            Optional<Mensalidade> mensalidadeExistente = mensalidadeRepository.findById(mensalidade.getId());

            assertThat(mensalidadeExistente.get().getStatus()).isEqualTo(StatusMensalidade.ATRASADA);
        }

        @Test
        void deveImpedirAtualizarMensalidadeMaisDeUmaVezNoMes() throws Exception {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);
            mensalidade.setAtualizacoes(1);

            mensalidadeRepository.save(mensalidade);

            mockMvc.perform(
                put("/mensalidade/atualizar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "diasTreino": 2
                        }
                        """)
            )
            .andExpect(status().isBadRequest());

            Optional<Mensalidade> mensalidadeExistente = mensalidadeRepository.findById(mensalidade.getId());
            
            assertThat(mensalidadeExistente.get().getDiasTreino()).isEqualTo(mensalidade.getDiasTreino());
        }

    }

    @Nested
    class buscarSuasMensalidadesTest {

        private Usuario usuario;
        private String token;
        
        @BeforeEach
        void prepararUsuarioComToken() {

            usuario = criarUsuario();
            token = gerarToken(usuario);
        }

        @Test
        void deveBuscarSuasMensalidadesComSucesso() throws Exception {

            Usuario usuario2 = criarUsuarioSemSalvar();
            usuario2.setEmail("teste@gmail.com");

            usuarioRepository.save(usuario2);   

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

            Mensalidade mensalidade3 = criarMensalidadePendente(usuario2);
            mensalidade3.setValor(BigDecimal.valueOf(65));

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2, mensalidade3);

            mensalidadeRepository.saveAll(mensalidades);

            mockMvc.perform(
                get("/mensalidade/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))     
            .andExpect(jsonPath("$.content[*].preco", containsInAnyOrder(45, 45))); 
        }

        @Test
        void deveRetornarPaginasVaziasSeNaoEncontrarMensalidades() throws Exception {

            mockMvc.perform(
                get("/mensalidade/me")
                .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.content").isEmpty());
        }   

    }

    @Nested
    class buscarTodasMensalidadesComFiltroTest {

        private Usuario usuario;
        private String token;
        
        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            token = gerarToken(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            mensalidadeRepository.saveAll(mensalidades);
        }

        @Test
        void deveBuscarTodasMensalidadesComSucesso() throws Exception {

            mockMvc.perform(
                get("/mensalidade/buscar")
                .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        void deveBuscarTodasMensalidadesComFiltroIntervaloDeDatas() throws Exception {

            mockMvc.perform(
                get("/mensalidade/buscar")
                .header("Authorization", "Bearer " + token)
                .param("diasTreino", "3")
                .param("dataCriacaoInicio", "2026-08-17")
                .param("dataCriacaoFim", "2026-08-21")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
        }

    }

    @Nested
    class buscarMensalidadesPorNomeTest {

        private Usuario usuario;
        private String token;
        
        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            token = gerarToken(usuario);

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            Mensalidade mensalidade2 = criarMensalidadePendente(usuario);

            List<Mensalidade> mensalidades = List.of(mensalidade, mensalidade2);

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            mensalidadeRepository.saveAll(mensalidades);
        }

        @Test
        void deveBuscarMensalidadesPorNomeUsuario() throws Exception {

            mockMvc.perform(
                get("/mensalidade/pesquisar")
                .header("Authorization", "Bearer " + token)
                .param("nome", "sau")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        void deveImpedirBuscarMensalidadesSemParametroNomeDoUsuario() throws Exception {

            mockMvc.perform(
                get("/mensalidade/pesquisar")
                .header("Authorization", "Bearer " + token) 
            )
            .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class pagarMensalidadeTest {

        private Usuario usuario;
        private String token;

        @BeforeEach
        void prepararSetup() {

            usuario = criarUsuario();
            token = gerarToken(usuario);
        }

        @Test
        void devePagarMensalidadeComSucesso() throws Exception {

            Mensalidade mensalidade = criarMensalidadePendente(usuario);

            mensalidadeRepository.save(mensalidade);
            
            mockMvc.perform(
                patch("/mensalidade/pagar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                            }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAGA"));
        }

    }

}
