package com.academia.auth.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Usuario.UsuarioAtualizarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioDeletarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @PutMapping("/editar")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(@Valid @RequestBody UsuarioAtualizarDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.atualizarUsuario(dto);

        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/listar")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
        @PageableDefault(size = 12, sort = "nome") Pageable pageable) {

        Page<UsuarioResponseDTO> usuarios = usuarioService.listarUsuarios(pageable);

        return ResponseEntity.ok(usuarios);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/promover-funcionario")
    public ResponseEntity<UsuarioResponseDTO> promoverUsuarioAFuncionario(@PathVariable Long id) {

        UsuarioResponseDTO usuario = usuarioService.promoverUsuarioAFuncionario(id);

        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/rebaixar-usuario")
    public ResponseEntity<UsuarioResponseDTO> rebaixarFuncionarioAUsuario(@PathVariable Long id) {

        UsuarioResponseDTO usuario = usuarioService.rebaixarFuncionarioAUsuario(id);

        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meusDados() {

        UsuarioResponseDTO usuario = usuarioService.meusDados();

        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @DeleteMapping
    public ResponseEntity<Void> deletarSeuUsuario(@Valid @RequestBody UsuarioDeletarDTO dto) {

        usuarioService.deletarUsuario(dto);

        return ResponseEntity.noContent().build();
    } 

}
