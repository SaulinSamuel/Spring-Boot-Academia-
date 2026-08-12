package com.academia.auth.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoRequestDTO;
import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoResponseDTO;
import com.academia.auth.Models.AvaliacaoFisica;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AvaliacaoFisicaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoFisicaServiceTest {

    @Mock
    private AvaliacaoFisicaRepository avaliacaoFisicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @InjectMocks
    private AvaliacaoFisicaService avaliacaoFisicaService;

    private Usuario usuario;

    @BeforeEach
    void configure() { 

        usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);
    }

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);

        return usuario;
    }

    @Nested
    class criarAvaliacaoFisicaTest {

        @Test
        void deveCriarAvaliacaoFisicaComSucesso() {

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario aluno = criarUsuario();

            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = hoje.withDayOfMonth(1);
            LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

            AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
            dto.setAltura(1.78);
            dto.setBraco(0.42);
            dto.setCintura(0.98);
            dto.setIdade(17);
            dto.setMassaMuscular(45.86);
            dto.setPeito(1.73);
            dto.setPercentualGordura(20.52);
            dto.setPeso(90.3);

            when(avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
                aluno, 
                inicioMes, 
                fimMes))
            .thenReturn(false);

            when(usuarioRepository.findById(aluno.getId()))
                .thenReturn(Optional.of(aluno));

            AvaliacaoResponseDTO resultado = avaliacaoFisicaService.criarAvaliacaoFisica(dto, aluno.getId());
        
            assertNotNull(resultado);

            assertEquals(dto.getPeso(), resultado.getPeso());
            assertEquals(usuario.getNome(), resultado.getAvaliador());
            assertEquals(aluno.getNome(), resultado.getAluno());

            ArgumentCaptor<AvaliacaoFisica> captor = ArgumentCaptor.forClass(AvaliacaoFisica.class);

            verify(avaliacaoFisicaRepository).save(captor.capture());

            AvaliacaoFisica avaliacaoFisicaCapturada = captor.getValue();

            assertEquals(usuario, avaliacaoFisicaCapturada.getAvaliador());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaCriarAvaliacoesFisicas() {

            

        }

    }

}
