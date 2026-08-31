package com.academia.auth.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Agendamento.AgendamentoResponseDTO;
import com.academia.auth.Services.AgendamentoService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {
    
    private final AgendamentoService agendamentoService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/criar")     
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(@PathVariable("id") Long aulaId) {

        AgendamentoResponseDTO agendamento = agendamentoService.criarAgendamento(aulaId);
    
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUTOR', 'FUNCIONARIO')")
    @GetMapping("/buscar/todos")
    public ResponseEntity<Page<AgendamentoResponseDTO>> buscarTodosAgendamentos(
        @PageableDefault(size = 12) Pageable pageable
    ) 
    {

        Page<AgendamentoResponseDTO> agendamentos = agendamentoService.buscarTodosAgendamentos(pageable);

        return ResponseEntity.ok(agendamentos);        
    }

    @PreAuthorize("hasRole('INSTRUTOR')")
    @GetMapping("/buscar/me")
    public ResponseEntity<Page<AgendamentoResponseDTO>> buscarSeusAgendamentos(
        @PageableDefault(size = 12) Pageable pageable
    )
    {

        Page<AgendamentoResponseDTO> agendamentos = agendamentoService.buscarSeusAgendamentos(pageable);
        
        return ResponseEntity.ok(agendamentos);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'INSTRUTOR', 'USER')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<AgendamentoResponseDTO> buscarAgendamentoPorId(@PathVariable("id") Long agendamentoId) {

        AgendamentoResponseDTO agendamento = agendamentoService.buscarAgendamentoPorId(agendamentoId);

        return ResponseEntity.ok(agendamento);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarAgendamento(@PathVariable("id") Long agendamentoId) {

        agendamentoService.cancelarAgendamento(agendamentoId);

        return ResponseEntity.noContent().build();
    }

}
