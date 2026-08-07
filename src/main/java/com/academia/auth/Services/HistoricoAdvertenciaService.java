package com.academia.auth.Services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.HistoricoAdvertencia.HistoricoAdvertenciaResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.HistoricoAdvertenciaMapper;
import com.academia.auth.Models.HistoricoAdvertencia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.HistoricoAdvertenciaRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.HistoricoAdvertenciaSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor    
@Service
public class HistoricoAdvertenciaService {
    
    private final HistoricoAdvertenciaRepository historicoAdvertenciaRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    public HistoricoAdvertenciaResponseDTO buscarHistoricoAdvertenciasPorId(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de visualizar esse histórico!");
        }

        HistoricoAdvertencia historicoAdvertencia = historicoAdvertenciaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Histórico não encontrado com esse usuário!"));

        return HistoricoAdvertenciaMapper.toDTO(historicoAdvertencia);
    }

    public Page<HistoricoAdvertenciaResponseDTO> buscarHistoricoAdvertenciasPorFiltro(
        String remetente,
        String destinatario,
        String excluidoPor,
        AdvertenciaStatus nivelAdvertencia,
        LocalDateTime inicio,
        LocalDateTime fim,
        Pageable pageable
    ) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de visualizar esse histórico!");
        }

        Specification<HistoricoAdvertencia> spec = (root, query, cb) -> null;
        
        spec.and(HistoricoAdvertenciaSpecification.dataExclusão(inicio, fim));
        spec.and(HistoricoAdvertenciaSpecification.destinatario(destinatario));
        spec.and(HistoricoAdvertenciaSpecification.excluidoPor(excluidoPor));
        spec.and(HistoricoAdvertenciaSpecification.nivelAdvertencia(nivelAdvertencia));
        spec.and(HistoricoAdvertenciaSpecification.remetente(remetente));

        Page<HistoricoAdvertencia> historicoAdvertencias = historicoAdvertenciaRepository.findAll(
            spec, pageable
        );

        return historicoAdvertencias
            .map(HistoricoAdvertenciaMapper::toDTO);
    }

}
