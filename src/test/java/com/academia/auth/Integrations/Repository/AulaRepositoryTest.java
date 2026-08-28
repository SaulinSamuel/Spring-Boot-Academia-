package com.academia.auth.Integrations.Repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;

@DataJpaTest
@Import(TestContainersConfig.class)
public class AulaRepositoryTest {
    
    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Aula criarAula(Usuario instrutor) {

        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.now().plusHours(2);
        LocalTime fim = inicio.plusHours(1);

        Aula aula = Aula.builder()
            .capacidadeInscricoes(10)
            .dataAula(hoje)
            .horarioInicio(inicio)
            .horarioFim(fim)
            .instrutor(instrutor)
            .status(StatusAula.PENDENTE)
            .nome("Aula treino inferiores")
        .build();

        return aula;
    }

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_INSTRUTOR);

        return usuario;
    }

    @Test
    void deveCancelarAulasComLimiteExcedido() {

        Usuario usuario = criarUsuario();
        usuarioRepository.save(usuario);

        Aula aula = criarAula(usuario);
        aulaRepository.save(aula);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime tempoLimite = agora.plusHours(2);

        aulaRepository.cancelarAulas(
            StatusAula.PENDENTE,
            StatusAula.CANCELADA, 
            tempoLimite
        );

        Optional<Aula> aulaCancelada = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario);
    
        assertThat(aulaCancelada.get().getStatus()).isEqualTo(StatusAula.CANCELADA);
    }

}
