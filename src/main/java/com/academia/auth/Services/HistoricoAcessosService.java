package com.academia.auth.Services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.HistoricoAcessos.HistoricoAcessosResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.HistoricoAcessosMapper;
import com.academia.auth.Models.HistoricoAcessos;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.HistoricoAcessosRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.HistoricoAcessosSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class HistoricoAcessosService {
    
    private final HistoricoAcessosRepository historicoAcessosRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    public Page<HistoricoAcessosResponseDTO> buscarTodosHistoricoAcessosPorFiltro(
        String nomeUsuario,
        DayOfWeek diaDaSemana,
        LocalDateTime inicio,
        LocalDateTime fim,
        Pageable pageable
    ) 
    {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para visualizar esses acessos!");
        }

        Specification<HistoricoAcessos> spec = (root, query, cb) -> null;

        spec = spec.and(HistoricoAcessosSpecification.diaDaSemana(diaDaSemana));
        spec = spec.and(HistoricoAcessosSpecification.horarioEntrada(inicio, fim));
        spec = spec.and(HistoricoAcessosSpecification.nomeUsuario(nomeUsuario));

        Page<HistoricoAcessos> historicoAcessos = historicoAcessosRepository.findAll(spec, pageable);

        return historicoAcessos
            .map(HistoricoAcessosMapper::toDTO);      
    }

    public HistoricoAcessosResponseDTO buscarHistoricoAcessoPorId(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem acesso para visualizar esse acesso!");
        }

        HistoricoAcessos historicoAcessos = historicoAcessosRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Historico de acesso não encontrado!"));

        return HistoricoAcessosMapper.toDTO(historicoAcessos);
    }

}
