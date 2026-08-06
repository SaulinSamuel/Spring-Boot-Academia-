package com.academia.auth.Controllers;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.HistoricoAdvertencia.HistoricoAdvertenciaResponseDTO;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Services.HistoricoAdvertenciaService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/historico")
public class HistoricoAdvertenciaController {
    
    private final HistoricoAdvertenciaService historicoAdvertenciaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping
    public ResponseEntity<Page<HistoricoAdvertenciaResponseDTO>> buscarHistoricoAdvertenciasPorFiltro(
        @RequestParam(required = false) String remetente,
        @RequestParam(required = false) String destinatario,
        @RequestParam(required = false) String excluidoPor,
        @RequestParam(required = false) AdvertenciaStatus nivelAdvertencia,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(required = false) LocalDateTime inicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(required = false) LocalDateTime fim,
        @PageableDefault(size = 12, sort = "remetente") Pageable pageable
    ) 
    {

        Page<HistoricoAdvertenciaResponseDTO> historicoAdvertencias = historicoAdvertenciaService.buscarHistoricoAdvertenciasPorFiltro(
            remetente, 
            destinatario, 
            excluidoPor,
            nivelAdvertencia, 
            inicio, 
            fim, 
            pageable
        );

        return ResponseEntity.ok(historicoAdvertencias);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<HistoricoAdvertenciaResponseDTO> buscarHistoricoAdvertenciasPorId(
        @PathVariable Long id
    ) 
    {

        HistoricoAdvertenciaResponseDTO dto = historicoAdvertenciaService.buscarHistoricoAdvertenciasPorId(id);

        return ResponseEntity.ok(dto);       
    }

}
