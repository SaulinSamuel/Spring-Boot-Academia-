package com.academia.auth.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeFilterDTO;
import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeResponseDTO;
import com.academia.auth.Services.HistoricoMensalidadeService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/historico-mensalidade")
public class HistoricoMensalidadeController {
    
    private final HistoricoMensalidadeService historicoMensalidadeService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping
    public ResponseEntity<Page<HistoricoMensalidadeResponseDTO>> buscarHistoricoMensalidade(
        HistoricoMensalidadeFilterDTO filter,
        @PageableDefault(size = 12, sort = "dataPagamento") Pageable pageable
    ) 
    {
    
        Page<HistoricoMensalidadeResponseDTO> historicoMensalidade = historicoMensalidadeService.buscarHistoricoDeMensalidades(
            filter,
            pageable
        );

        return ResponseEntity.ok(historicoMensalidade);
    }

}
