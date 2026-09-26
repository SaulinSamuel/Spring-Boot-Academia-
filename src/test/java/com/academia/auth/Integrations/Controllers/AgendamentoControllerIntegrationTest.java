package com.academia.auth.Integrations.Controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;
import com.academia.auth.factories.UsuarioFactoryTest;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@SpringBootTest
@Transactional
public class AgendamentoControllerIntegrationTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AulaRepository aulaRepository;
    
    private UsuarioFactoryTest usuarioFactoryTest = new UsuarioFactoryTest();

    private Aula criarAula(Usuario instrutor) {

        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.of(13, 30);
        LocalTime fim = inicio.plusHours(1);

        Aula aula = Aula.builder()
            .capacidadeInscricoes(10)
            .dataAula(hoje)
            .horarioInicio(inicio)
            .horarioFim(fim)
            .quantidadeInscritos(2)
            .instrutor(instrutor)
            .status(StatusAula.PENDENTE)
            .nome("Aula treino inferiores")
        .build();

        return aula;
    }

    @Nested
    class CriarAgendamentoTest {

        private Usuario aluno;
        private Usuario instrutor;
        private Aula aula;

        @BeforeEach
        void prepararSetup() {

            aluno = usuarioFactoryTest.criarUsuarioSemId();

            instrutor = usuarioFactoryTest.criarUsuarioSemId();
            instrutor.setEmail("instrutor@gmail.com");
            instrutor.setTelefone("(99) 93473-3713");

            usuarioRepository.saveAll(List.of(aluno, instrutor));

            aula = criarAula(instrutor);
            aulaRepository.save(aula);
        }

        @Test 
        void deveCriarAgendamento() throws Exception {

            mockMvc.perform(
                post("/agendamento/" + aula.getId() + "/criar")
                .with(user(aluno))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nomeAula").value(aula.getNome()));
        }   

        @Test 
        void deveImpedirSemPermissao() throws Exception {

            aluno.setRole(RoleUser.ROLE_FUNCIONARIO);

            mockMvc.perform(
                post("/agendamento/" + aula.getId() + "/criar")
                .with(user(aluno))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
        }

    }

}
