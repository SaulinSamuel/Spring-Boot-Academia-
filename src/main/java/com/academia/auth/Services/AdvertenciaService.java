package com.academia.auth.Services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Advertencia.AdvertenciaRequestDTO;
import com.academia.auth.DTOS.Advertencia.AdvertenciaResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AdvertenciaMapper;
import com.academia.auth.Mappers.HistoricoAdvertenciaMapper;
import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.HistoricoAdvertencia;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.AdvertenciaStatus;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AdvertenciaRepository;
import com.academia.auth.Repositories.HistoricoAdvertenciaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.AdvertenciaSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdvertenciaService {
    
    private final AdvertenciaRepository advertenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioLogado;
    private final HistoricoAdvertenciaRepository historicoAdvertenciaRepository;

    @Transactional
    public AdvertenciaResponseDTO enviarAdvertencia(AdvertenciaRequestDTO dto, Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de enviar advertências");
        }

        Usuario usuarioDestinatario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Usuário não encontrado!"));
        
        if (usuarioDestinatario.getRole() != RoleUser.ROLE_USER) {
            throw new BusinessException("Você não pode enviar advertências para funcionários!");
        }

        Long quantidadeAdvertencias = advertenciaRepository.countByDestinatario(usuarioDestinatario);

        if (quantidadeAdvertencias >= 3) {
            throw new BusinessException("Este usuário já possui 3 advertências!");
        }

        Advertencia advertencia = AdvertenciaMapper.toEntity(dto);

        advertencia.setRemetente(usuario);
        advertencia.setDestinatario(usuarioDestinatario);
        advertencia.setDataCriacao(LocalDateTime.now());
        definirDataExpiracao(advertencia);
        
        advertenciaRepository.save(advertencia);

        return AdvertenciaMapper.toDTO(advertencia);
    }

    public Page<AdvertenciaResponseDTO> mostrarSuasAdvertenciasRecebidas(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Page<Advertencia> advertencias = advertenciaRepository.findAllByDestinatario(usuario, pageable);

        return advertencias
            .map(AdvertenciaMapper::toDTO);
    }   

    public Page<AdvertenciaResponseDTO> mostrarSuasAdvertenciasEnviadas(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para visualizar essas advertências!");
        }

        Page<Advertencia> advertencias = advertenciaRepository.findAllByRemetente(usuario, pageable);
        
        return advertencias
            .map(AdvertenciaMapper::toDTO);
    }

    public Page<AdvertenciaResponseDTO> buscarTodasAdvertenciasPorFiltro(
        String remetente, 
        String destinatario,
        AdvertenciaStatus nivelAdvertencia,
        LocalDateTime inicio,
        LocalDateTime fim,
        Pageable pageable
    ) 
    {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de visualizar as advertências!");
        }

        Specification<Advertencia> spec = (root, query, cb) -> null;

        spec = spec.and(AdvertenciaSpecification.dataCriacao(inicio, fim));
        spec = spec.and(AdvertenciaSpecification.nivelAdvertencia(nivelAdvertencia));
        spec = spec.and(AdvertenciaSpecification.remetente(remetente));
        spec = spec.and(AdvertenciaSpecification.destinatario(destinatario));

        Page<Advertencia> advertencias = advertenciaRepository.findAll(spec, pageable);

        return advertencias
            .map(AdvertenciaMapper::toDTO);
    }

    public AdvertenciaResponseDTO buscarAdvertenciaPorId(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de visualizar essa advertência!");
        }

        Advertencia advertencia = advertenciaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Advertência não encontrada!"));

        return AdvertenciaMapper.toDTO(advertencia);

    }

    @Transactional
    public void excluirAdvertencia(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Advertencia advertencia = advertenciaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Advertência não encontrada!"));

        if (!advertencia.getRemetente().getId().equals(usuario.getId()) &&
            usuario.getRole() != RoleUser.ROLE_ADMIN) 
        {
            throw new BusinessException("Você não tem permissão para excluir essa advertência!");
        }

        HistoricoAdvertencia historicoAdvertencia = HistoricoAdvertenciaMapper.toEntityFromAdvertencia(
            advertencia, 
            usuario
        );

        historicoAdvertenciaRepository.save(historicoAdvertencia);

        advertenciaRepository.delete(advertencia);
    }

    private void definirDataExpiracao(Advertencia advertencia) {

        if (advertencia.getNivelAdvertencia() == AdvertenciaStatus.LEVE) {

            advertencia.setDataExpiracao(LocalDateTime.now().plusDays(3));

        } else if (advertencia.getNivelAdvertencia() == AdvertenciaStatus.MODERADA) {

            advertencia.setDataExpiracao(LocalDateTime.now().plusDays(6));

        } else if (advertencia.getNivelAdvertencia() == AdvertenciaStatus.GRAVE) {

            advertencia.setDataExpiracao(LocalDateTime.now().plusDays(9));

        }
    }

}
