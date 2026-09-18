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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Notificacao.NotificacaoResponseDTO;
import com.academia.auth.Services.NotificacaoService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@RestController 
@RequestMapping("/notificacao")
public class NotificacaoController {
    
    private final NotificacaoService notificacaoService;

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUTOR', 'FUNCIONARIO', 'USER')")
    @PatchMapping("/{notificacaoId}/ler")
    public ResponseEntity<NotificacaoResponseDTO> marcarNotificacaoComoLida(
        @PathVariable("notificacaoId") Long notificacaoId
    ) 
    {
        return ResponseEntity.ok(notificacaoService.marcarNotificacaoComoLida(notificacaoId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUTOR', 'FUNCIONARIO', 'USER')")
    @GetMapping("/me")
    public ResponseEntity<Page<NotificacaoResponseDTO>> buscarMinhasNotificacoes(
        @PageableDefault(size = 12, sort = "tipoNotificacao") Pageable pageable
    ) 
    {
        return ResponseEntity.ok(notificacaoService.buscarMinhasNotificacoes(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUTOR', 'FUNCIONARIO', 'USER')")
    @GetMapping("/{notificacaoId}/buscar")
    public ResponseEntity<NotificacaoResponseDTO> buscarNotificacaoPorId(
        @PathVariable("notificacaoId") Long notificacaoId
    ) 
    {
        return ResponseEntity.ok(notificacaoService.buscarNotificacaoPorId(notificacaoId));
    }

    @DeleteMapping("/{notificacaoId}/deletar") 
    public ResponseEntity<Void> deletarNotificacao(
        @PathVariable("notificacaoId") Long notificacaoId
    ) 
    {
        notificacaoService.deletarNotificacao(notificacaoId);

        return ResponseEntity.noContent().build();
    }

}
