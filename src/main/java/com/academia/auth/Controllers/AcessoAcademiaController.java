package com.academia.auth.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.Services.AcessoAcademiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/acesso")
public class AcessoAcademiaController {
    
    private final AcessoAcademiaService academiaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    public ResponseEntity<Void> acessarAcademia(@Valid @RequestBody AcessoAcademiaRequestDTO dto) {

        academiaService.entrarAcademia(dto);

        return ResponseEntity.ok().build();
    }

}
