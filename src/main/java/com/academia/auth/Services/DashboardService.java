package com.academia.auth.Services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.Dashboard.DashboardResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardService {
    
    private final UsuarioRepository usuarioRepository;
    private final MensalidadeRepository mensalidadeRepository;
    private final AcessoAcademiaRepository acessoAcademiaRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    public DashboardResponseDTO buscarDadosDashboard() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para buscar dados dashboard!");
        }

        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        Long quantidadeAlunos = usuarioRepository.countByRole(RoleUser.ROLE_USER);
        Long mensalidadesPendentes = mensalidadeRepository.countByStatus(StatusMensalidade.PENDENTE);
        Long mensalidadesPagas = mensalidadeRepository.countByStatus(StatusMensalidade.PAGA);
        Long mensalidadesCanceladas = mensalidadeRepository.countByStatus(StatusMensalidade.CANCELADA);

        BigDecimal faturamento = Optional.ofNullable(
            mensalidadeRepository.somarValorPorPeriodo(StatusMensalidade.PAGA, 
                inicio, 
                fim
            )
        ).orElse(BigDecimal.ZERO);
        
        Long quantidadeFuncionarios = usuarioRepository.countByRole(RoleUser.ROLE_FUNCIONARIO);
        Long acessosSemana = acessoAcademiaRepository.somarDiasAcessadosSemana();

        DashboardResponseDTO dashboard = DashboardResponseDTO.builder()
            .acessosSemana(acessosSemana)
            .faturamentoTotal(faturamento)
            .mensalidadesPagas(mensalidadesPagas)
            .mensalidadesPendentes(mensalidadesPendentes)
            .mensalidadesCanceladas(mensalidadesCanceladas)
            .quantidadeAlunos(quantidadeAlunos)
            .quantidadeFuncionarios(quantidadeFuncionarios)
        .build();

        return dashboard;
    }

}
