package com.academia.auth.Services;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.Exceptions.AcessoAcademiaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Utils.StatusMensalidade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AcessoAcademiaService {
    
    private final AcessoAcademiaRepository acessoAcademiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MensalidadeRepository mensalidadeRepository;

    @Transactional
    public void entrarAcademia(AcessoAcademiaRequestDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResourceNotFound("Aluno não encontrado!"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new BusinessException("Senha incorreta!");
        }

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            throw new AcessoAcademiaException("Mensalidade em atraso ou cancelada!");
        }

        AcessoAcademia acessoAcademia = usuario.getAcessosAcademia();

        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);

        if (hoje.isEqual(hoje.with(DayOfWeek.SATURDAY)) || hoje.isEqual(hoje.with(DayOfWeek.SUNDAY))) {
            throw new AcessoAcademiaException("Academia não é aberta aos sábados e domingos!");
        }

        if (!inicioSemana.equals(acessoAcademia.getInicioSemana())) {
            acessoAcademia.setInicioSemana(inicioSemana);
            acessoAcademia.setDiasAcesso(0);
        }   

        if (LocalDate.now().equals(acessoAcademia.getUltimoAcesso())) {
            throw new AcessoAcademiaException("Você já acessou a academia hoje!");
        }

        if (acessoAcademia.getDiasAcesso() >= mensalidade.getDiasTreino()) {
            throw new AcessoAcademiaException("Máximo de dias de treino na semana atingidos!");
        }

        acessoAcademia.setUltimoAcesso(hoje);
        acessoAcademia.setDiasAcesso(acessoAcademia.getDiasAcesso() + 1);

        acessoAcademiaRepository.save(acessoAcademia);

        System.out.println("Acesso concedido!");
    }

}
