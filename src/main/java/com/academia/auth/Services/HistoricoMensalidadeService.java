package com.academia.auth.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeFilterDTO;
import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Mappers.HistoricoMensalidadeMapper;
import com.academia.auth.Models.HistoricoMensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.HistoricoMensalidadeRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.HistoricoMensalidadeSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class HistoricoMensalidadeService {

    private final HistoricoMensalidadeRepository historicoMensalidadeRepository;

    private final UsuarioAutenticadoService usuarioLogado;

    public Page<HistoricoMensalidadeResponseDTO> buscarHistoricoDeMensalidades(
        HistoricoMensalidadeFilterDTO filter,
        Pageable pageable) 
        {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para visualizar historico de mensalidade!");
        }

        log.info(">>> Filtro recebido: {}", filter);

        Specification<HistoricoMensalidade> spec = HistoricoMensalidadeSpecification.filtroHistoricoMensalidades(filter);

        Page<HistoricoMensalidade> historicoMensalidade = historicoMensalidadeRepository.findAll(spec, pageable);

        log.info(">>> Total encontrado: {}", historicoMensalidade.getTotalElements());

        return historicoMensalidade
            .map(HistoricoMensalidadeMapper::toDTO);
    }

}
