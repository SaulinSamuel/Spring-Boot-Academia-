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
        LocalTime inicio = LocalTime.of(10, 0);
        
        Aula aula = Aula.builder()
            .capacidadeInscricoes(10)
            .dataAula(hoje)
            .horarioInicio(inicio)
            .horarioFim(inicio.plusHours(2))
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
        aulaRepository.saveAndFlush(aula);

        LocalDate hoje = LocalDate.now();
        LocalDateTime tempoLimite = LocalDateTime.of(hoje, LocalTime.of(12, 0));

        int linhasAfetadas = aulaRepository.cancelarAulas(
            StatusAula.PENDENTE,
            StatusAula.CANCELADA,
            tempoLimite
        );

        Optional<Aula> aulaCancelada = aulaRepository.findById(aula.getId());
    
        assertThat(linhasAfetadas).isEqualTo(1);
        assertThat(aulaCancelada.get().getStatus()).isEqualTo(StatusAula.CANCELADA);
    }

}
