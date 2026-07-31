package com.academia.auth.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academia.auth.DTOS.Dashboard.DashboardResponseDTO;
import com.academia.auth.Services.DashboardService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    @GetMapping("/buscar")
    public ResponseEntity<DashboardResponseDTO> buscarDadosDashboard() {

        DashboardResponseDTO dashboard = dashboardService.buscarDadosDashboard();

        return ResponseEntity.ok(dashboard);
    }
}
