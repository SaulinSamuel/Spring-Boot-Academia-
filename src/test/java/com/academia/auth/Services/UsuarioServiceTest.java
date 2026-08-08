package com.academia.auth.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.academia.auth.DTOS.Usuario.UsuarioAtualizarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
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

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioComSucesso() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO(
            "Saulo",
            "saulo@email.com",
            "1243345"
        );

        when(usuarioRepository.existsByEmail(dto.getEmail()))
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

        UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
            "Saulo novo",
            "saulonovo@email.com",
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
}
