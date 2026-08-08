package com.academia.auth.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Models.Usuario;
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
        verify(usuarioRepository).save(any(Usuario.class));
    }
    
    @Test
    void deveLancarExcecaoEmailDuplicado() {

        
    }
}
