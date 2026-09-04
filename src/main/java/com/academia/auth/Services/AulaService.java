package com.academia.auth.Services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Aula.AulaFilterDTO;
import com.academia.auth.DTOS.Aula.AulaRequestDTO;
import com.academia.auth.DTOS.Aula.AulaResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.AulaException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AulaMapper;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusAula;
import com.academia.auth.Repositories.AulaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.AulaSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AulaService {
    
    private final AulaRepository aulaRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional
    public AulaResponseDTO criarAula(AulaRequestDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR) {
            throw new AulaException("Você não tem permissão para criar aulas!");
        }

        Optional<Aula> aulaExistente = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario);

        if (aulaExistente.isPresent() &&
            aulaExistente.get().getStatus() != StatusAula.CONCLUIDA &&
            aulaExistente.get().getStatus() != StatusAula.CANCELADA) 
        {
            throw new AulaException("Você não pode criar aulas enquanto não concluir atual!");
        }

        Aula aula = AulaMapper.toEntity(dto);

        aula.setInstrutor(usuario);
        aula.setStatus(StatusAula.PENDENTE);

        aulaRepository.save(aula);

        return AulaMapper.toDTO(aula);
    }

    @Transactional
    public AulaResponseDTO atualizarAula(AulaRequestDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR) {
            throw new AulaException("Você não tem permissão para criar aulas!");
        }

        Aula aula = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));

        if (aula.getStatus() != StatusAula.PENDENTE) {
            throw new BusinessException("Você não pode atualizar aulas não pendentes!");
        }

        aula.setCapacidadeInscricoes(dto.capacidadeInscricoes());
        aula.setDataAula(dto.dataAula());
        aula.setHorarioFim(dto.horarioFim());
        aula.setHorarioInicio(dto.horarioInicio());
        aula.setNome(dto.nome());

        aulaRepository.save(aula);

        return AulaMapper.toDTO(aula);
    }

    @Transactional
    public AulaResponseDTO confirmarAula() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR) {
            throw new AulaException("Você não tem permissão para confirmar aulas!");
        }

        Aula aula = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));

        if (aula.getStatus() != StatusAula.PENDENTE) {
            throw new AulaException("Apenas aulas pendentes podem ser confirmadas!");
        }

        aula.setStatus(StatusAula.CONFIRMADA);

        aulaRepository.save(aula);

        return AulaMapper.toDTO(aula);
    }

    @Transactional
    public AulaResponseDTO cancelarAula() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR) {
            throw new AulaException("Você não tem permissão para cancelar aulas!");
        }

        Aula aula = aulaRepository.findTopByInstrutorOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));

        if (aula.getStatus() != StatusAula.PENDENTE) {
            throw new AulaException("Apenas aulas pendentes podem ser canceladas!");
        }

        aula.setStatus(StatusAula.CANCELADA);

        aulaRepository.save(aula);

        return AulaMapper.toDTO(aula);
    }

    @Transactional(readOnly = true)
    public Page<AulaResponseDTO> buscarTodasAulas(
        AulaFilterDTO filter,
        Pageable pageable) 
    {

        Specification<Aula> specification = AulaSpecification.filter(filter);

        Page<Aula> aulas = aulaRepository.findAll(specification, pageable);

        return aulas
            .map(AulaMapper::toDTO);
    }

    public Page<AulaResponseDTO> buscarAulasCriadasPorInstrutor(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR)
        {
            throw new BusinessException("Você não tem permissão de visualizar aulas!");
        }

        Page<Aula> aulas = aulaRepository.findAllByInstrutor(usuario, pageable);

        return aulas
            .map(AulaMapper::toDTO);
    }

    public AulaResponseDTO buscarAulaPorId(Long id) {

        Aula aula = aulaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));

        return AulaMapper.toDTO(aula);
    }

    @Transactional
    public void excluirAula(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_INSTRUTOR) {
            throw new BusinessException("Você não tem permissão para excluir aulas!");
        }

        Aula aula = aulaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Aula não encontrada!"));

        if (!aula.getInstrutor().getId().equals(usuario.getId())) {
            throw new BusinessException("Você não tem permissão de excluir essa aula!");
        }

        if (aula.getStatus() != StatusAula.CONCLUIDA &&
            aula.getStatus() != StatusAula.CANCELADA)
        {
            throw new BusinessException("Apenas aulas concluidas(ou canceladas) podem ser excluidas!");
        }

        aulaRepository.delete(aula);
    }

}
