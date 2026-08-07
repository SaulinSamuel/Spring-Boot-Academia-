package com.academia.auth.Services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoRequestDTO;
import com.academia.auth.DTOS.AvaliacaoFisica.AvaliacaoResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.AvaliacaoFisicaMapper;
import com.academia.auth.Models.AvaliacaoFisica;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AvaliacaoFisicaRepository;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.AvaliacaoFisicaSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AvaliacaoFisicaService {
    
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional
    public AvaliacaoResponseDTO criarAvaliacaoFisica(AvaliacaoRequestDTO dto, Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para criar avaliações fisícas!");
        }

        Usuario aluno = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Aluno não encontrado!"));

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        if (avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
            usuario, inicioMes, fimMes)
        )
        {
            throw new BusinessException("Avaliação física só pode ser feita uma vez no mês por aluno!");
        }

        AvaliacaoFisica avaliacaoFisica = AvaliacaoFisicaMapper.toEntity(dto);

        avaliacaoFisica.setDataAvaliacao(hoje);
        avaliacaoFisica.setAluno(aluno);
        avaliacaoFisica.setAvaliador(usuario);

        avaliacaoFisicaRepository.save(avaliacaoFisica);
        
        return AvaliacaoFisicaMapper.toDTO(avaliacaoFisica);
    }

    public Page<AvaliacaoResponseDTO> buscarSuasAvaliacaoFisica(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAllByAluno(usuario, pageable);

        return avaliacoesFisicas
            .map(AvaliacaoFisicaMapper::toDTO);
    }

    public Page<AvaliacaoResponseDTO> buscarSuasAvaliacoesFisicasCriadas(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAllByAvaliador(
            usuario, 
            pageable
        );

        return avaliacoesFisicas
            .map(AvaliacaoFisicaMapper::toDTO);
    }

    public Page<AvaliacaoResponseDTO> buscarTodasAvaliacoesFisicasPorFiltro(
        String aluno,
        String avaliador,
        Integer idade,
        LocalDate inicio,
        LocalDate fim,
        Pageable pageable
    ) 
    {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão para visualizar todas as avaliações!");
        }

        Specification<AvaliacaoFisica> spec = (root, query, cb) -> null;

        spec = spec.and(AvaliacaoFisicaSpecification.aluno(aluno));
        spec = spec.and(AvaliacaoFisicaSpecification.avaliador(avaliador));
        spec = spec.and(AvaliacaoFisicaSpecification.dataAvaliacao(inicio, fim));
        spec = spec.and(AvaliacaoFisicaSpecification.idade(idade));

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAll(spec, pageable);

        return avaliacoesFisicas
            .map(AvaliacaoFisicaMapper::toDTO);
    }

    public AvaliacaoResponseDTO buscarAvaliacaoFisicaPorId(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            throw new BusinessException("Você não tem permissão de visualizar essa avaliação fisiíca!");
        }

        AvaliacaoFisica avaliacaoFisica = avaliacaoFisicaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Avaliação física não  encontrado!"));
            
        return AvaliacaoFisicaMapper.toDTO(avaliacaoFisica);       
    }

    @Transactional
    public void excluirAvaliacaoFisica(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        AvaliacaoFisica avaliacaoFisica = avaliacaoFisicaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Avaliação física não encontrada!"));

        boolean isAdmin = usuario.getRole() == RoleUser.ROLE_ADMIN;
        boolean isAvaliador = avaliacaoFisica.getAvaliador().equals(usuario.getId());
    
        if (!isAdmin && !isAvaliador) {
            throw new BusinessException("Você não tem permissão para excluir essa avaliação física!");
        }

        avaliacaoFisicaRepository.delete(avaliacaoFisica);
    }

}
