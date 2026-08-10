package com.academia.auth.Controllers;

import java.time.LocalDate;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoRequestDTO;
import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoResponseDTO;
import com.academia.auth.Services.AvaliacaoFisicaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/avaliacao-fisica")
public class AvaliacaoFisicaController {

    private final AvaliacaoFisicaService avaliacaoFisicaService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PostMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> criarAvaliacaoFisica(
        @RequestBody @Valid AvaliacaoRequestDTO dto,
        @PathVariable Long id
    ) 
    {

        AvaliacaoResponseDTO avaliacaoFisica = avaliacaoFisicaService.criarAvaliacaoFisica(dto, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoFisica);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> editarAvaliacaoFisica(
        @RequestBody @Valid AvaliacaoRequestDTO dto,
        @PathVariable Long id
    ) 
    {
        
        AvaliacaoResponseDTO avaliacaoFisica = avaliacaoFisicaService.editarAvaliacaoFisica(dto, id);
    
        return ResponseEntity.ok(avaliacaoFisica);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO', 'USER')")
    @GetMapping("/me")
    public ResponseEntity<Page<AvaliacaoResponseDTO>> buscarSuasAvaliacoesFisicas(
        @PageableDefault(size = 12, sort = "dataAvaliacao") Pageable pageable
    ) 
    {

        Page<AvaliacaoResponseDTO> avaliacoesFisicas = avaliacaoFisicaService
            .buscarSuasAvaliacaoFisicaAlunos(pageable);
        
        return ResponseEntity.ok(avaliacoesFisicas);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/me/criadas")
    public ResponseEntity<Page<AvaliacaoResponseDTO>> buscarSuasAvaliacoesFisicasCriadas(
        @PageableDefault(size = 12, sort = "dataAvaliacao") Pageable pageable
    ) 
    {   

        Page<AvaliacaoResponseDTO> avaliacoesFisicas = avaliacaoFisicaService.buscarSuasAvaliacoesFisicasCriadas(pageable);

        return ResponseEntity.ok(avaliacoesFisicas);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/{id}/buscar")
    public ResponseEntity<AvaliacaoResponseDTO> buscarAvaliacaoFisicaPorId(@PathVariable Long id) {

        AvaliacaoResponseDTO avaliacaoFisica = avaliacaoFisicaService.buscarAvaliacaoFisicaPorId(id);

        return ResponseEntity.ok(avaliacaoFisica);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/pesquisar")
    public ResponseEntity<Page<AvaliacaoResponseDTO>> buscarTodasAvaliacoesFisicasPorFiltro(
        @RequestParam(required = false) String aluno,
        @RequestParam(required = false) String avaliador,
        @RequestParam(required = false) Integer idade,
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim,
        @PageableDefault(size = 12, sort = "dataAvaliacao") Pageable pageable
    ) 
    {

        Page<AvaliacaoResponseDTO> avaliacoesFisicas = avaliacaoFisicaService.buscarTodasAvaliacoesFisicasPorFiltro(aluno, 
            avaliador, 
            idade, 
            inicio, 
            fim, 
            pageable
        );

        return ResponseEntity.ok(avaliacoesFisicas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAvaliacaoFisica(@PathVariable Long id) {

        avaliacaoFisicaService.excluirAvaliacaoFisica(id);

        return ResponseEntity.noContent().build();
    }

}
