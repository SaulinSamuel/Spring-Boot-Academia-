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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Services.MensalidadeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/mensalidade")
public class MensalidadeController {
    
    private final MensalidadeService mensalidadeService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @PostMapping
    public ResponseEntity<MensalidadeResponseDTO> criarMensalidade(@Valid @RequestBody MensalidadeRequestDTO dto) {

        MensalidadeResponseDTO mensalidade = mensalidadeService.criarMensalidade(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(mensalidade);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @PutMapping("/atualizar")
    public ResponseEntity<MensalidadeResponseDTO> atualizarMensalidade(@Valid @RequestBody MensalidadeRequestDTO dto) {

        MensalidadeResponseDTO mensalidade = mensalidadeService.atualizarMensalidade(dto);

        return ResponseEntity.ok(mensalidade);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @GetMapping("/me")
    public ResponseEntity<Page<MensalidadeResponseDTO>> buscarSuasMensalidades(@PageableDefault(size = 12) Pageable pageable) {

        Page<MensalidadeResponseDTO> mensalidades = mensalidadeService.buscarSuasMensalidades(pageable);

        return ResponseEntity.ok(mensalidades);
    }

    @PreAuthorize("hasRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/buscar")
    public ResponseEntity<Page<MensalidadeResponseDTO>> buscarTodasMensalidades(@PageableDefault(size = 12) Pageable pageable) {

        Page<MensalidadeResponseDTO> mensalidades = mensalidadeService.buscarTodasMensalidades(pageable);

        return ResponseEntity.ok(mensalidades);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @PatchMapping("/pagar")
    public ResponseEntity<MensalidadeResponseDTO> pagarMensalidade() {

        MensalidadeResponseDTO mensalidade = mensalidadeService.pagarMensalidade();

        return ResponseEntity.ok(mensalidade);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FUNCIONARIO')")
    @PatchMapping("/cancelar")
    public ResponseEntity<MensalidadeResponseDTO> cancelarMensalidade() {

        MensalidadeResponseDTO mensalidade = mensalidadeService.cancelarMensalidade();

        return ResponseEntity.ok(mensalidade);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @DeleteMapping("/deletar")
    public ResponseEntity<Void> excluirMensalidade() {

        mensalidadeService.excluirMensalidade();

        return ResponseEntity.noContent().build();
    }
}
