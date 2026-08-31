package com.academia.auth.Services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Agendamento.AgendamentoResponseDTO;
import com.academia.auth.Exceptions.AgendamentoException;
import com.academia.auth.Exceptions.AulaException;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AgendamentoMapper;
import com.academia.auth.Models.Agendamento;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAgendamento;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AgendamentoRepository;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AgendamentoService {
    
    private final AgendamentoRepository agendamentoRepository;
    private final AulaRepository aulaRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional  
    public AgendamentoResponseDTO criarAgendamento(Long aulaId) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para criar agendamentos!");
        }

        Aula aula = aulaRepository.findById(aulaId)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));
        
        if (aula.getStatus() != StatusAula.PENDENTE) {
            throw new AulaException("Agendamentos disponíveis apenas para aulas pendentes!");
        }

        boolean existeAgendamentoIdEStatusAula = agendamentoRepository.existeMaisDeUmAgendamentoComStatus(
            usuario.getId(),
            aulaId,
            StatusAula.PENDENTE  
        );

        if (existeAgendamentoIdEStatusAula) {
            throw new AulaException("Você já tem um agendamento em uma aula pendente!");
        }
        
        LocalDateTime agora = LocalDateTime.now();

        Agendamento agendamento = new Agendamento();
        agendamento.setAula(aula);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento.setUsuario(usuario);
        agendamento.setDataAgendamento(agora);

        agendamentoRepository.save(agendamento);

        return AgendamentoMapper.toDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponseDTO> buscarTodosAgendamentos(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de buscar agendamentos!");
        }

        Page<Agendamento> agendamentos = agendamentoRepository.findAll(pageable);
        
        return agendamentos
            .map(AgendamentoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponseDTO> buscarSeusAgendamentos(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Page<Agendamento> agendamentos = agendamentoRepository.findAllByUsuario(usuario, pageable);

        return agendamentos
            .map(AgendamentoMapper::toDTO);       
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarAgendamentoPorId(Long agendamentoId) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
            .orElseThrow(() -> new ResourceNotFound("Agendamento não enontrado!"));
        
        if (!agendamento.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Você não tem permissão para visualizar esse agendamento!");
        }

        return AgendamentoMapper.toDTO(agendamento);
    }

    @Transactional
    public void cancelarAgendamento(Long agendamentoId) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de cancelar agendamentos!");
        }

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
            .orElseThrow(()  -> new ResourceNotFound("Agendamento não encontrado!"));
        
        Aula aula = agendamento.getAula();
            
        if (!agendamento.getUsuario().getId().equals(usuario.getId())) {
            throw new AgendamentoException("Você não tem permissão de cancelar esse agendamento!");
        }

        if (aula.getStatus() != StatusAula.PENDENTE) {
            throw new AulaException("Você não pode cancelar agendamento em aula não pendente!");
        }

        agendamentoRepository.delete(agendamento);
    }

}
