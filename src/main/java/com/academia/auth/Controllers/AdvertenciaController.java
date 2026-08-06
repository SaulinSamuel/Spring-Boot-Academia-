package com.academia.auth.Controllers;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Advertencia.AdvertenciaRequestDTO;
import com.academia.auth.DTOS.Advertencia.AdvertenciaResponseDTO;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Services.AdvertenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/advertencia")
public class AdvertenciaController {
    
    private final AdvertenciaService advertenciaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/{id}/enviar")
    public ResponseEntity<AdvertenciaResponseDTO> enviarAdvertencia(
    @PathVariable Long id,
    @RequestBody @Valid AdvertenciaRequestDTO dto) {

        AdvertenciaResponseDTO advertencia = advertenciaService.enviarAdvertencia(dto, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(advertencia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/pesquisar")
    public ResponseEntity<Page<AdvertenciaResponseDTO>> buscarAdvertenciasPorFiltro(
        @RequestParam(required = false) String remetente,
        @RequestParam(required = false) String destinatario,
        @RequestParam(required = false) AdvertenciaStatus nivelAdvertencia,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(required = false) LocalDateTime inicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(required = false) LocalDateTime fim,
        @PageableDefault(size = 12, sort = "nivelAdvertencia") Pageable pageable
    ) {

        Page<AdvertenciaResponseDTO> advertencias = advertenciaService.buscarTodasAdvertenciasPorFiltro(
            remetente, 
            destinatario, 
            nivelAdvertencia, 
            inicio, 
            fim, 
            pageable
        );

        return ResponseEntity.ok(advertencias);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<AdvertenciaResponseDTO> buscarAdvertenciaPorId(@PathVariable Long id) {

        AdvertenciaResponseDTO advertencia = advertenciaService.buscarAdvertenciaPorId(id);

        return ResponseEntity.ok(advertencia);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'USER')")
    @GetMapping("/recebidas/me")
    public ResponseEntity<Page<AdvertenciaResponseDTO>> mostrarSuasAdvertenciasRecebidas(
        @PageableDefault(size = 12, sort = "destinatario") Pageable pageable
    ) {

        Page<AdvertenciaResponseDTO> advertencias = advertenciaService.mostrarSuasAdvertenciasRecebidas(pageable);

        return ResponseEntity.ok(advertencias);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/enviadas/me")
    public ResponseEntity<Page<AdvertenciaResponseDTO>> mostrarSuasAdvertenciasEnviadas(
        @PageableDefault(size = 12, sort = "remetente") Pageable pageable
    ) {

        Page<AdvertenciaResponseDTO> advertencias = advertenciaService.mostrarSuasAdvertenciasEnviadas(pageable);

        return ResponseEntity.ok(advertencias);    
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @DeleteMapping("/{id}/deletar")
    public ResponseEntity<Void> excluirAdvertencia(@PathVariable Long id) {

        advertenciaService.excluirAdvertencia(id);

        return ResponseEntity.noContent().build();
    }

}
