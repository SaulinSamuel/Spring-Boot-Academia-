package com.academia.auth.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.academia.auth.DTOS.Aula.AulaRequestDTO;
import com.academia.auth.DTOS.Aula.AulaResponseDTO;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class AulaServiceTest {
    
    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @InjectMocks
    private AulaService aulaService;

    private Usuario usuario;

    @BeforeEach
    void configure() { 

        usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_INSTRUTOR);
    }

    private Aula criarAula(Usuario instrutor) {

        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.of(13, 30);
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

    private AulaRequestDTO criarAulaRequest() {
    
        LocalDate hoje = LocalDate.now();
        LocalTime inicio = LocalTime.of(13, 30);
        LocalTime fim = inicio.plusHours(1);

        AulaRequestDTO dto = new AulaRequestDTO(
            "Aula superiores",
            hoje,
            inicio,
            fim,
            11
        );

        return dto;
    }

    @Nested
    class criarAulaTest {

        @Test
        void deveCriarAulaComSucesso() {

            AulaRequestDTO dto = criarAulaRequest();

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);
            
            when(aulaRepository.findTopByInstrutorOrderByIdDesc(usuario))
                .thenReturn(Optional.empty());
            
            AulaResponseDTO resultado = aulaService.criarAula(dto);

            assertThat(resultado.instrutor()).isEqualTo(usuario.getNome());

            verify(aulaRepository).save(any(Aula.class));
        }

        

    }

}
