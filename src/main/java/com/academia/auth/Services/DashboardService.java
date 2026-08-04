package com.academia.auth.Services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.Dashboard.DashboardResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;

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
        Long mensalidadePendentes = mensalidadeRepository.countByStatus(StatusMensalidade.PENDENTE);
        Long mensalidadesPagas = mensalidadeRepository.countByStatus(StatusMensalidade.PAGA);
        BigDecimal faturamento = mensalidadeRepository.somarValorPorPeriodo(
            StatusMensalidade.PAGA,
            inicio,
            fim
        );
        Long quantidadeFuncionarios = usuarioRepository.countByRole(RoleUser.ROLE_FUNCIONARIO);
        Long acessosSemana = acessoAcademiaRepository.somarDiasAcessadosSemana();

        DashboardResponseDTO dashboard = new DashboardResponseDTO();

        dashboard.setAcessosSemana(acessosSemana);
        dashboard.setFaturamentoTotal(faturamento);
        dashboard.setMensalidadesPagas(mensalidadesPagas);
        dashboard.setMensalidadesPendentes(mensalidadePendentes);
        dashboard.setQuantidadeFuncionarios(quantidadeFuncionarios);
        dashboard.setQuantidadeAlunos(quantidadeAlunos);

        return dashboard;
    }

}
