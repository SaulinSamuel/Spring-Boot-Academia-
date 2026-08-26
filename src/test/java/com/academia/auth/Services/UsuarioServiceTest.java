package com.academia.auth.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.academia.auth.DTOS.Usuario.UsuarioAtualizarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioDeletarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UsuarioAutenticadoService usuarioLogado;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AcessoAcademiaRepository acessoAcademiaRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Saulin");
        usuario.setEmail("saulo@gmail.com");
        usuario.setSenha("091812");
        usuario.setRole(RoleUser.ROLE_USER);
        
        return usuario;
    }

    @Nested
    class CadastrarUsuarioTest {
        @Test
        void deveCadastrarUsuarioComSucesso() {

            UsuarioRequestDTO dto = new UsuarioRequestDTO(
                "Saulo",
                "saulo@email.com",
                "98 98382 2833",
                "1243345"
            );

            when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);

            when(usuarioRepository.existsByTelefone(dto.getTelefone()))
                .thenReturn(false);

            when(passwordEncoder.encode(dto.getSenha()))
                .thenReturn("senha-criptografada");

            Usuario usuarioSalvo = new Usuario();

            usuarioSalvo.setId(1L);
            usuarioSalvo.setNome(dto.getNome());
            usuarioSalvo.setEmail(dto.getEmail());
            usuarioSalvo.setSenha("senha-criptografada");

            when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioSalvo);
            
            UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(dto);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("Saulo", resultado.getNome());
            assertEquals("saulo@email.com", resultado.getEmail());

            verify(passwordEncoder).encode("1243345");
            
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            verify(usuarioRepository).save(captor.capture());

            Usuario usuarioEnviado = captor.getValue();

            assertEquals("Saulo", usuarioEnviado.getNome());
            assertEquals("saulo@email.com", usuarioEnviado.getEmail());
            assertEquals("senha-criptografada", usuarioEnviado.getSenha());
        }
        
        @Test
        void deveLancarExcecaoEmailDuplicado() {

            UsuarioRequestDTO dto = new UsuarioRequestDTO(
                "Saulin",
                "saulo@gmail.com",
                "98 83733 9784",
                "091812"
            );

            when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);

            assertThrows(
                BusinessException.class,
                () -> usuarioService.cadastrarUsuario(dto)
            );

            verify(usuarioRepository, never())
                .save(any(Usuario.class));
        }
    }

    @Nested
    class AtualizarUsuarioTest {

        @Test
        void deveAtualizarComSucessoSemTrocarSenha() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);
        
            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(
                "123456",
                "091812"
            )).thenReturn(true);

            when(usuarioRepository.existsByEmailAndIdNot(
                "saulonovo@email.com",
                1L
            )).thenReturn(false);

            when(usuarioRepository.existsByTelefoneAndIdNot(
                "98 87483 7643",
                1L
            )).thenReturn(false);

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
                "Saulo novo",
                "saulonovo@email.com",
                "98 87483 7643",
                "123456",
                null
            );

            when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

            UsuarioResponseDTO resultado = usuarioService.atualizarUsuario(dto);

            assertNotNull(resultado);

            assertEquals(1L, resultado.getId());
            assertEquals("Saulo novo", resultado.getNome());
            assertEquals("saulonovo@email.com", resultado.getEmail());

            assertEquals(
                "091812",
                usuario.getSenha()
            );

            verify(usuarioRepository).save(usuario);
        }

        @Test
        void deveInformarSenhaIncorreta() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO();
            dto.setEmail("saulonovo@gmail.com");
            dto.setSenhaAtual("123456");
            dto.setNome("Saulo novo");
            dto.setSenhaNova(null);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(
                dto.getSenhaAtual(),
                usuario.getSenha()
            )).thenReturn(false);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.atualizarUsuario(dto)
            );

            assertEquals("Senha incorreta!", exception.getMessage());
            
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        void deveInformarEmailJaExistententeAoAtualizar() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO();
            dto.setEmail("saulonovo@gmail.com");
            dto.setSenhaAtual("091812");
            dto.setNome("Saulo novo");
            dto.setSenhaNova(null);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(
                dto.getSenhaAtual(),
                usuario.getSenha()
            )).thenReturn(true);

            when(usuarioRepository.existsByEmailAndIdNot(
                dto.getEmail(),
                1L
            )).thenReturn(true);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.atualizarUsuario(dto)
            );

            assertEquals("Usuário já existente com esse email!", exception.getMessage());

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        void deveAtualizarTrocandoDeSenha () {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO();
            dto.setEmail("saulo@gmail.com");
            dto.setSenhaAtual("091812");
            dto.setNome("Saulo");
            dto.setSenhaNova("123456");

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(
                dto.getSenhaAtual(),
                usuario.getSenha()
            )).thenReturn(true);

            when(passwordEncoder.encode("123456"))
                .thenReturn("senhacript");

            when(usuarioRepository.existsByEmailAndIdNot(
                dto.getEmail(),
                1L
            )).thenReturn(false);

            when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

            UsuarioResponseDTO resultado = usuarioService.atualizarUsuario(dto);

            assertNotNull(resultado);
            
            assertEquals(1L, resultado.getId());
            assertEquals(dto.getEmail(), resultado.getEmail());
            assertEquals(dto.getNome(), resultado.getNome());

            verify(passwordEncoder).encode("123456");

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            verify(usuarioRepository).save(captor.capture());

            Usuario usuarioEnviado = captor.getValue();

            assertEquals("Saulo", usuarioEnviado.getNome());
            assertEquals("saulo@gmail.com", usuarioEnviado.getEmail());
            assertEquals("senhacript", usuarioEnviado.getSenha());
        }
    }

    @Nested
    class promoverUsuarioAFuncionarioTest {

        @Test
        void devePromoverUsuarioAFuncionarioComSucesso() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_ADMIN);

            Usuario futuroFuncionario = new Usuario();

            futuroFuncionario.setEmail("joao@gmail.com");
            futuroFuncionario.setNome("Joao");
            futuroFuncionario.setId(2L);
            futuroFuncionario.setSenha("123456");
            futuroFuncionario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(futuroFuncionario));

            when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(futuroFuncionario);

            UsuarioResponseDTO resultado = usuarioService.promoverUsuarioAFuncionario(futuroFuncionario.getId());
            Optional<AcessoAcademia> acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario);

            assertEquals(futuroFuncionario.getEmail(), resultado.getEmail());
            assertEquals(futuroFuncionario.getNome(), resultado.getNome());
            assertEquals(futuroFuncionario.getId(), resultado.getId());
            assertEquals(RoleUser.ROLE_FUNCIONARIO, resultado.getRole());
            assertThat(acessoAcademia).isNotEmpty();

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            verify(usuarioRepository).save(captor.capture());

            Usuario usuarioCapturado = captor.getValue();

            assertEquals(RoleUser.ROLE_FUNCIONARIO, usuarioCapturado.getRole());
        }

        @Test
        void deveLancarExcecaoSemPermissaoAoPromoverUsuario() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            Usuario futuroFuncionario = new Usuario();

            futuroFuncionario.setEmail("joao@gmail.com");
            futuroFuncionario.setNome("Joao");
            futuroFuncionario.setId(2L);
            futuroFuncionario.setSenha("123456");
            futuroFuncionario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.promoverUsuarioAFuncionario(futuroFuncionario.getId())
            );

            assertEquals("Você não tem permissão para promover usuário a funcionário!", exception.getMessage());

            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

    }

    @Nested
    class rebaixarFuncionarioAUsuarioTest {

        @Test
        void deveRebaixarFuncionarioAUsuarioComSucesso () {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_ADMIN);

            Usuario usuarioRebaixado = new Usuario();

            usuarioRebaixado.setEmail("joao@gmail.com");
            usuarioRebaixado.setNome("Joao");
            usuarioRebaixado.setId(2L);
            usuarioRebaixado.setSenha("123456");
            usuarioRebaixado.setRole(RoleUser.ROLE_FUNCIONARIO);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuarioRebaixado));

            when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioRebaixado);

            UsuarioResponseDTO resultado = usuarioService.rebaixarFuncionarioAUsuario(usuarioRebaixado.getId());

            assertNotNull(resultado);

            assertEquals(usuarioRebaixado.getId(), resultado.getId());
            assertEquals(usuarioRebaixado.getNome(), resultado.getNome());
            assertEquals(RoleUser.ROLE_USER, resultado.getRole());

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

            verify(usuarioRepository).save(captor.capture());

            Usuario usuarioCapturado = captor.getValue();

            assertEquals(RoleUser.ROLE_USER, usuarioCapturado.getRole());
        }

        @Test
        void deveLancarExcecaoSemPermissaoAoRebaixarFuncionario() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuarioRebaixado = new Usuario();

            usuarioRebaixado.setEmail("joao@gmail.com");
            usuarioRebaixado.setNome("Joao");
            usuarioRebaixado.setId(2L);
            usuarioRebaixado.setSenha("123456");
            usuarioRebaixado.setRole(RoleUser.ROLE_FUNCIONARIO);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.rebaixarFuncionarioAUsuario(usuarioRebaixado.getId())
            );

            assertEquals("Você não tem permissão para rebaixar funcionários!", exception.getMessage());

            verify(usuarioRepository, never()).save(usuarioRebaixado);          
        }

    }

    @Nested
    class listarUsuariosTest {

        @Test
        void deveListarUsuariosComSucesso() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = new Usuario();

            usuario2.setId(2L);
            usuario2.setNome("Saulo");
            usuario2.setEmail("saulin@gmail.com");
            usuario2.setSenha("091812");
            usuario2.setRole(RoleUser.ROLE_FUNCIONARIO);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            List<Usuario> usuarios = List.of(usuario, usuario2);

            Page<Usuario> page = new PageImpl<>(usuarios);
            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioRepository.findAll(pageable))
                .thenReturn(page);

            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios(pageable);

            assertNotNull(resultado);

            assertEquals(usuario.getId(), resultado.getContent().get(0).getId());
            assertEquals(usuario.getEmail(), resultado.getContent().get(0).getEmail());
            assertEquals(usuario.getNome(), resultado.getContent().get(0).getNome());

            assertEquals(2, resultado.getContent().size());
            assertEquals(page.getSize(), resultado.getSize());
            assertEquals(page.getTotalPages(), resultado.getTotalPages());
        }
    
        @Test
        void deveLancarExcecaoSemPermissaoParaListarUsuarios() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.listarUsuarios(pageable)
            );

            assertEquals("Você não tem permissão para ver os usuários!", exception.getMessage());
        }
    }

    @Nested
    class listarUsuariosPorNomeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioLogado() {

            usuario = criarUsuario();
            
            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);
        }

        @Test
        void deveListarUsuariosPorNomeComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = criarUsuario();  
            usuario2.setEmail("teste@gmail.com");

            Usuario usuario3 = criarUsuario();
            usuario3.setEmail("teste2@gmail.com");

            List<Usuario> usuarios = List.of(usuario, usuario2, usuario3);
            Page<Usuario> page = new PageImpl<>(usuarios);

            Pageable pageable = PageRequest.of(0, 10);

            when(usuarioRepository.findAllByNomeContainingIgnoreCase(
                "sau",
                pageable
            )).thenReturn(page);

            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuariosPorNome("sau", pageable);

            assertThat(resultado).isNotEmpty();
            
            assertThat(resultado.getContent()).extracting(UsuarioResponseDTO::getId)
                .containsExactlyInAnyOrder(usuario.getId(), usuario2.getId(), usuario3.getId());
        } 

        @Test
        void deveLancarExcecaoSemPermissaoParaVisualizarTodosUsuarios() {

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.listarUsuariosPorNome("sau", pageable)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para visualizar todos usuarios!");

            verify(usuarioRepository, never()).findAllByNomeContainingIgnoreCase("sau", pageable);
        }

    }

    @Nested
    class retornarMeusDadosTest {

        @Test
        void deveRetornarMeusDadosComSucesso() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            UsuarioResponseDTO resultado = usuarioService.meusDados();

            assertNotNull(resultado);
            assertEquals(usuario.getId(), resultado.getId());
            assertEquals(usuario.getEmail(), resultado.getEmail());
            assertEquals(usuario.getNome(), resultado.getNome());
        }

    }

    @Nested
    class deletarUsuarioTest {

        @Test
        void deveExcluirUsuarioComSucesso() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            UsuarioDeletarDTO dto = new UsuarioDeletarDTO(
                "091812"
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha()))
                .thenReturn(true);
            
            usuarioService.deletarUsuario(dto);

            assertNotNull(dto);
        }
    
        @Test
        void deveLancarExcecaoSenhaIncorreta() {

            Usuario usuario = new Usuario();

            usuario.setId(1L);
            usuario.setNome("Saulin");
            usuario.setEmail("saulo@gmail.com");
            usuario.setSenha("091812");
            usuario.setRole(RoleUser.ROLE_USER);

            UsuarioDeletarDTO dto = new UsuarioDeletarDTO(
                "123456"
            );

            when(usuarioLogado.usuarioLogado())
                .thenReturn(usuario);

            when(passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha()))
                .thenReturn(false);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.deletarUsuario(dto)
            );

            assertEquals("Senha incorreta!", exception.getMessage());
        }

    }

}
