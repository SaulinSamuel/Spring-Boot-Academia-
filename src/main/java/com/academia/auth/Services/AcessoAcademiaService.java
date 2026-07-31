package com.academia.auth.Services;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaResponseDTO;
import com.academia.auth.Exceptions.AcessoAcademiaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AcessoAcademiaMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
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
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional
    public AcessoAcademiaResponseDTO acessarAcademia(AcessoAcademiaRequestDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResourceNotFound("Aluno não encontrado!"));

        log.info("Usuário {} entrou em acessar academia", usuario.getEmail());

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            log.warn("Usuário {} errou sua senha ao acessar academia!", usuario.getEmail());
            throw new BusinessException("Senha incorreta!");
        }
        if (usuario.getRole() != RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou acessar a academia como aluno!", usuario.getEmail());
            throw new AcessoAcademiaException("Este acesso é somente para alunos!");
        }

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            log.warn("Usuário {} tentou entrar na academia com mensalidade invalidada", usuario.getEmail());
            throw new AcessoAcademiaException("Mensalidade em atraso ou cancelada!");
        }

        AcessoAcademia acessoAcademia = usuario.getAcessosAcademia();

        LocalDate hoje = validarAcessoAcademiaAluno(acessoAcademia, mensalidade, usuario);

        acessoAcademia.setUltimoAcesso(hoje);
        acessoAcademia.setDiasAcesso(acessoAcademia.getDiasAcesso() + 1);

        acessoAcademiaRepository.save(acessoAcademia);
        log.info("Usuário {} acessou a academia as {}", usuario.getEmail(), hoje);

        return AcessoAcademiaMapper.toDTO(acessoAcademia);
    }

    @Transactional
    public AcessoAcademiaResponseDTO acessarAcademiaFuncionario(AcessoAcademiaRequestDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResourceNotFound("Usuário não encontrado!"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            log.warn("Funcionário {} errou sua senha!", usuario.getEmail());
            throw new BusinessException("Senha incorreta!");
        }
        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou acessar como funcionário!", usuario.getEmail());
            throw new AcessoAcademiaException("Apenas funcionários podem utlizar esse acesso!");
        }

        AcessoAcademia acessoAcademia = usuario.getAcessosAcademia();

        LocalDate hoje = LocalDate.now();

        if (hoje.getDayOfWeek() == DayOfWeek.SATURDAY ||
            hoje.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.warn("Usuário {} tentou entrar na academia fechada!", usuario.getEmail());
            throw new AcessoAcademiaException("A academia é fechada aos sábados e domingos!");
        }
        if (!acessoAcademia.getInicioSemana().equals(hoje.with(DayOfWeek.MONDAY))) {
            acessoAcademia.setDiasAcesso(0);
            acessoAcademia.setInicioSemana(hoje.with(DayOfWeek.MONDAY));
        }

        acessoAcademia.setDiasAcesso(acessoAcademia.getDiasAcesso() + 1);
        acessoAcademia.setUltimoAcesso(hoje);

        acessoAcademiaRepository.save(acessoAcademia);
        log.info("Funcionário {} acessou a academia!", usuario.getEmail());

        return AcessoAcademiaMapper.toDTO(acessoAcademia);
    }

    public AcessoAcademiaResponseDTO buscarSeuAcesso() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        AcessoAcademia acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario)
            .orElseThrow(() -> new ResourceNotFound("Acesso da academia não encontrado!"));

        return AcessoAcademiaMapper.toDTO(acessoAcademia);
    }

    public Page<AcessoAcademiaResponseDTO> buscarTodosAcesso(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para visualizar esses acessos!");
        }

        Page<AcessoAcademia> acessosAcademia = acessoAcademiaRepository.findAll(pageable);

        if (acessosAcademia.isEmpty()) {
            throw new ResourceNotFound("Acessos não encontrados!");
        }

        return acessosAcademia
            .map(AcessoAcademiaMapper::toDTO);
    }

    public Page<AcessoAcademiaResponseDTO> buscarAcessoPorNome(Pageable pageable, String nome) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para buscar acessos!");
        }

        Page<AcessoAcademia> acessosAcademia = acessoAcademiaRepository.findByNomeContainingIgnoreCase(pageable, nome);

        return acessosAcademia
            .map(AcessoAcademiaMapper::toDTO);
    }

    public LocalDate validarAcessoAcademiaAluno(AcessoAcademia acessoAcademia, Mensalidade mensalidade, Usuario usuario) {
        
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);

        if (hoje.isEqual(hoje.with(DayOfWeek.SATURDAY)) || hoje.isEqual(hoje.with(DayOfWeek.SUNDAY))) {
            log.warn("Usuário {} tentou entrar na academia no sábado e domingo", usuario.getEmail());
            throw new AcessoAcademiaException("Academia não é aberta aos sábados e domingos!");
        }

        if (!inicioSemana.equals(acessoAcademia.getInicioSemana())) {
            acessoAcademia.setInicioSemana(inicioSemana);
            acessoAcademia.setDiasAcesso(0);
        }   

        if (LocalDate.now().equals(acessoAcademia.getUltimoAcesso())) {
            log.warn("Usuário {} tentou acessar mais de uma vez no dia", usuario.getEmail());
            throw new AcessoAcademiaException("Você já acessou a academia hoje!");
        }

        if (acessoAcademia.getDiasAcesso() >= mensalidade.getDiasTreino()) {
            log.warn("Usuário {} tentou acessar mais de {} vezes na semana", usuario.getEmail(), mensalidade.getDiasTreino());
            throw new AcessoAcademiaException("Máximo de dias de treino na semana atingidos!");
        }

        return hoje;
    }
}
