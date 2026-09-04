package com.academia.auth.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Aula.AulaFilterDTO;
import com.academia.auth.DTOS.Aula.AulaRequestDTO;
import com.academia.auth.DTOS.Aula.AulaResponseDTO;
import com.academia.auth.Services.AulaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/aula")
public class AulaController {
    
    private final AulaService aulaService;

    @PreAuthorize("hasRole('INSTRUTOR')")
    @PostMapping
    public ResponseEntity<AulaResponseDTO> criarAula(@RequestBody @Valid AulaRequestDTO dto) {

        AulaResponseDTO aula = aulaService.criarAula(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(aula);
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @PutMapping
    public ResponseEntity<AulaResponseDTO> atualizarAula(@RequestBody @Valid AulaRequestDTO dto) {

        AulaResponseDTO aula = aulaService.atualizarAula(dto);

        return ResponseEntity.ok(aula);
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @PatchMapping("/confirmar")
    public ResponseEntity<AulaResponseDTO> confirmarAula() {

        AulaResponseDTO aula = aulaService.confirmarAula();

        return ResponseEntity.ok(aula);
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @PatchMapping("/cancelar")
    public ResponseEntity<AulaResponseDTO> cancelarAula() {

        AulaResponseDTO aula = aulaService.cancelarAula();

        return ResponseEntity.ok(aula);
    }

    @PreAuthorize("hasAnyRole('USER', 'FUNIONARIO', 'INSTRUTOR', 'ADMIN')")
    @GetMapping("/buscar/todas")
    public ResponseEntity<Page<AulaResponseDTO>> buscarTodasAulas(
        AulaFilterDTO filter,
        @PageableDefault(size = 12) Pageable pageable
    )
    {

        Page<AulaResponseDTO> aulas = aulaService.buscarTodasAulas(filter, pageable);

        return ResponseEntity.ok(aulas);
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @GetMapping("/me")
    public ResponseEntity<Page<AulaResponseDTO>> buscarAulasCriadasPorInstrutor(
        @PageableDefault(size = 12) Pageable pageable
    ) 
    {

        Page<AulaResponseDTO> aulas = aulaService.buscarAulasCriadasPorInstrutor(pageable);

        return ResponseEntity.ok(aulas);
    }

    @PreAuthorize("hasAnyRole('USER', 'FUNCIONARIO', 'INSTRUTOR', 'ADMIN')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<AulaResponseDTO> buscarAulaPorId(@PathVariable Long id) {

        AulaResponseDTO aula = aulaService.buscarAulaPorId(id);

        return ResponseEntity.ok(aula);
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @DeleteMapping("/{id}/excluir")
    public ResponseEntity<Void> excluirAula(@PathVariable Long id) {

        aulaService.excluirAula(id);

        return ResponseEntity.noContent().build();
    }

}
