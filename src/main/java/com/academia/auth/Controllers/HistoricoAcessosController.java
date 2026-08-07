package com.academia.auth.Controllers;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.HistoricoAcessos.HistoricoAcessosResponseDTO;
import com.academia.auth.Services.HistoricoAcessosService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/historico-acesso")
public class HistoricoAcessosController {
    
    private final HistoricoAcessosService historicoAcessosService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping
    public ResponseEntity<Page<HistoricoAcessosResponseDTO>> buscarTodosHistoricoAcessosPorFiltro(
        @RequestParam(required = false) String nomeUsuario,
        @RequestParam(required = false) LocalDateTime inicio,
        @RequestParam(required = false) LocalDateTime fim,
        @RequestParam(required = false) DayOfWeek diaDaSemana,
        @PageableDefault(size = 12, sort = "nomeUsuario") Pageable pageable
    ) 
    {

        Page<HistoricoAcessosResponseDTO> historicoAcessos = historicoAcessosService.buscarTodosHistoricoAcessosPorFiltro(
            nomeUsuario, 
            diaDaSemana, 
            inicio, 
            fim, 
            pageable
        );

        return ResponseEntity.ok(historicoAcessos);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<HistoricoAcessosResponseDTO> buscarHistoricoAcessosPorId(
        @PathVariable Long id
    )
    {

        HistoricoAcessosResponseDTO historicoAcessos = historicoAcessosService.buscarHistoricoAcessoPorId(id);

        return ResponseEntity.ok(historicoAcessos);
    }

}
