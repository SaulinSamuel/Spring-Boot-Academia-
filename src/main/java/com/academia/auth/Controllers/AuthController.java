package com.academia.auth.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Login.LoginRequestDTO;
import com.academia.auth.DTOS.Login.LoginResponseDTO;
import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Services.UsuarioService;
import com.academia.auth.Services.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UsuarioService usuarioService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(
        @Valid @RequestBody UsuarioRequestDTO dto
    ) {

        UsuarioResponseDTO usuario = usuarioService.cadastrarUsuario(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {

        LoginResponseDTO login = authService.login(dto);

        return ResponseEntity.ok(login);
    }

}
