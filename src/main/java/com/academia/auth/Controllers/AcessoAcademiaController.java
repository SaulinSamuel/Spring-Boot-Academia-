package com.academia.auth.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaResponseDTO;
import com.academia.auth.Services.AcessoAcademiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/acesso")
public class AcessoAcademiaController {
    
    private final AcessoAcademiaService academiaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/alunos")
    public ResponseEntity<AcessoAcademiaResponseDTO> acessarAcademia(@RequestBody @Valid AcessoAcademiaRequestDTO dto) {

        AcessoAcademiaResponseDTO acessoAcademia = academiaService.acessarAcademia(dto);

        return ResponseEntity.ok(acessoAcademia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/funcionarios")
    public ResponseEntity<AcessoAcademiaResponseDTO> acessarAcademiaFuncionario(@RequestBody @Valid AcessoAcademiaRequestDTO dto) {

        AcessoAcademiaResponseDTO acessoAcademia = academiaService.acessarAcademiaFuncionario(dto);

        return ResponseEntity.ok(acessoAcademia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'USER')")
    @GetMapping("/me")
    public ResponseEntity<AcessoAcademiaResponseDTO> buscarSeuAcesso() {

        AcessoAcademiaResponseDTO acessoAcademia = academiaService.buscarSeuAcesso();

        return ResponseEntity.ok(acessoAcademia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/buscar")
    public ResponseEntity<Page<AcessoAcademiaResponseDTO>> buscarTodosAcessos(@PageableDefault(size = 12, sort = "usuario") Pageable pageable) {

        Page<AcessoAcademiaResponseDTO> acessosAcademia = academiaService.buscarTodosAcesso(pageable);

        return ResponseEntity.ok(acessosAcademia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/pesquisar")
    public ResponseEntity<Page<AcessoAcademiaResponseDTO>> buscarAcessosPorNome(
        @PageableDefault(size = 12, sort = "nome") Pageable pageable,
        @RequestParam String nome
    ) {

        Page<AcessoAcademiaResponseDTO> acessosAcademia = academiaService.buscarAcessoPorNome(pageable, nome);

        return ResponseEntity.ok(acessosAcademia);
    }
}
