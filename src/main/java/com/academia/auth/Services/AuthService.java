package com.academia.auth.Services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.Login.LoginRequestDTO;
import com.academia.auth.DTOS.Login.LoginResponseDTO;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Security.JwtService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO dto) {

        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    dto.getEmail(),
                    dto.getSenha()
                )
            );
        
        Usuario usuario = (Usuario) authentication.getPrincipal();

        String token = jwtService.gerarToken(usuario);

        return new LoginResponseDTO(token);
    }

}
