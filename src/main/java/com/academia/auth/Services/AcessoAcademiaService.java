package com.academia.auth.Services;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaRequestDTO;
import com.academia.auth.DTOS.AcessoAcademia.AcessoAcademiaResponseDTO;
import com.academia.auth.Events.AcessarAcademiaEvent;
import com.academia.auth.Exceptions.AcessoAcademiaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AcessoAcademiaMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.AdvertenciaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AcessoAcademiaService {
    
    private final AcessoAcademiaRepository acessoAcademiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdvertenciaRepository advertenciaRepository;
    private final PasswordEncoder passwordEncoder;
    private final MensalidadeRepository mensalidadeRepository;
    private final UsuarioAutenticadoService usuarioLogado;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

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

        validarAdvertenciasAluno(usuario);

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            log.warn("Usuário {} tentou entrar na academia com mensalidade invalidada", usuario.getEmail());
            throw new AcessoAcademiaException("Mensalidade em atraso ou cancelada!");
        }

        AcessoAcademia acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario)
            .orElseThrow(() -> new ResourceNotFound("Acesso academia não encontrado!"));

        validarAcessoAcademiaAluno(acessoAcademia, mensalidade, usuario);

        LocalDate hoje = LocalDate.now(clock);

        acessoAcademia.setUltimoAcesso(hoje);
        acessoAcademia.setDiasAcesso(acessoAcademia.getDiasAcesso() + 1);

        acessoAcademiaRepository.save(acessoAcademia);
        log.info("Usuário {} acessou a academia as {}", usuario.getEmail(), hoje);

        applicationEventPublisher.publishEvent(
            new AcessarAcademiaEvent(acessoAcademia)
        );

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

        AcessoAcademia acessoAcademia = acessoAcademiaRepository.findByUsuario(usuario)
            .orElseThrow(() -> new ResourceNotFound("Acesso não encontrado!"));

        validarAcessoAcademiaFuncionario(acessoAcademia, usuario);

        LocalDate hoje = LocalDate.now(clock);

        acessoAcademia.setDiasAcesso(acessoAcademia.getDiasAcesso() + 1);
        acessoAcademia.setUltimoAcesso(hoje);

        acessoAcademiaRepository.save(acessoAcademia);
        log.info("Funcionário {} acessou a academia!", usuario.getEmail());

        applicationEventPublisher.publishEvent(
            new AcessarAcademiaEvent(acessoAcademia)
        );

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

    public void validarAcessoAcademiaAluno(
        AcessoAcademia acessoAcademia, 
        Mensalidade mensalidade, 
        Usuario usuario) 
    {
        
        if (!acessoAcademia.getUsuario().getId().equals(usuario.getId())) {
            throw new AcessoAcademiaException("Esse acesso não pertence a você!");
        }

        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);
        DayOfWeek dia = hoje.getDayOfWeek();

        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            log.warn("Usuário {} tentou entrar na academia no sábado e domingo", usuario.getEmail());
            throw new AcessoAcademiaException("Academia não é aberta aos sábados e domingos!");
        }

        if (!inicioSemana.equals(acessoAcademia.getInicioSemana())) {
            acessoAcademia.setInicioSemana(inicioSemana);
            acessoAcademia.setDiasAcesso(0);
        }

        if (hoje.equals(acessoAcademia.getUltimoAcesso())) {
            log.warn("Usuário {} tentou acessar mais de uma vez no dia", usuario.getEmail());
            throw new AcessoAcademiaException("Você já acessou a academia hoje!");
        }

        if (acessoAcademia.getDiasAcesso() >= mensalidade.getDiasTreino()) {
            log.warn("Usuário {} tentou acessar mais de {} vezes na semana", usuario.getEmail(), mensalidade.getDiasTreino());
            throw new AcessoAcademiaException("Máximo de dias de treino na semana atingidos!");
        }
        
    }

    public void validarAcessoAcademiaFuncionario(AcessoAcademia acessoAcademia, Usuario usuario) {

        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);
        DayOfWeek dia = hoje.getDayOfWeek();
        
        if (!acessoAcademia.getUsuario().getId().equals(usuario.getId())) {
            log.warn("Usuário {} tentou acessar com acesso inválido id: {}", usuario.getEmail(), acessoAcademia.getId());
            throw new AcessoAcademiaException("Esse acesso de academia não pertence a você!");
        }
        
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            log.warn("Usuário {} tentou entrar na academia no sábado e domingo", usuario.getEmail());
            throw new AcessoAcademiaException("Academia não é aberta aos sábados e domingos!");
        }

        if (!inicioSemana.equals(acessoAcademia.getInicioSemana())) {
            acessoAcademia.setInicioSemana(inicioSemana);
            acessoAcademia.setDiasAcesso(0);
        }   

        if (hoje.equals(acessoAcademia.getUltimoAcesso())) {
            log.warn("Usuário {} tentou acessar mais de uma vez no dia", usuario.getEmail());
            throw new AcessoAcademiaException("Você já acessou a academia hoje!");
        }

    }

    public void validarAdvertenciasAluno(Usuario usuario) {
        
        List<Advertencia> advertencias = advertenciaRepository.findAllByDestinatario(usuario);

        long total = advertencias.size();

        long graves = advertencias.stream()
            .filter(a -> a.getNivelAdvertencia() == AdvertenciaStatus.GRAVE)
            .count();

        long moderadas = advertencias.stream()
            .filter(a -> a.getNivelAdvertencia() == AdvertenciaStatus.MODERADA)
            .count();
        
        if (graves >= 1) {
            throw new AcessoAcademiaException("Você não pode acessar a academia com 1 advertência grave ou mais!");
        } else if (moderadas >= 2) {
            throw new AcessoAcademiaException("Você não pode acessar a academia com 2 advertências moderadas ou mais!");
        } else if (total >= 3) {
            throw new AcessoAcademiaException("Você não pode acessar a academia com 3 advertências ou mais!");
        }

        log.info("Advertencias de aluno validadas acesso liberado!");
    }

}
