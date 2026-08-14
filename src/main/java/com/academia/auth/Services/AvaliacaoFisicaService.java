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
        log.info("Usuário {} criando avaliação fisíca", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou criar avaliação física sem permissão", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para criar avaliações físicas!");
        }

        Usuario aluno = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Aluno não encontrado!"));

        if (aluno.getRole() != RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou criar avaliação física em funcionários!", usuario.getEmail());
            throw new BusinessException("Não é permitido fazer avaliações físicas em funcionários!");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        if (avaliacaoFisicaRepository.existsByAlunoAndDataAvaliacaoBetween(
            aluno, inicioMes, fimMes)
        )
        {
            log.warn("Usuário {} tentou criar avaliação física mais de uma vez no mês!", usuario.getEmail());
            throw new BusinessException("Avaliação física só pode ser feita uma vez no mês por aluno!");
        }

        AvaliacaoFisica avaliacaoFisica = AvaliacaoFisicaMapper.toEntity(dto);
        
        avaliacaoFisica.setDataAvaliacao(hoje);
        avaliacaoFisica.setAluno(aluno);
        avaliacaoFisica.setAvaliador(usuario);

        avaliacaoFisicaRepository.save(avaliacaoFisica);
        
        return AvaliacaoFisicaMapper.toDTO(avaliacaoFisica);
    }

    @Transactional
    public AvaliacaoResponseDTO editarAvaliacaoFisica(AvaliacaoRequestDTO dto, Long id) {
        
        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em editar avaliação", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou editar avaliação sem permissão!", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para editar avaliações fisícas!");
        }

        AvaliacaoFisica avaliacaoFisica = avaliacaoFisicaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Avaliação fisíca não encontrada!"));

        if (!(usuario.getRole() == RoleUser.ROLE_ADMIN) && 
            !avaliacaoFisica.getAvaliador().getId().equals(usuario.getId())) 
        {
            log.warn("Usuário {} tentou editar avaliação física sem permissão!", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para editar essa avaliação física!");
        }

        avaliacaoFisica.setAltura(dto.getAltura());
        avaliacaoFisica.setBraco(dto.getBraco());
        avaliacaoFisica.setCintura(dto.getCintura());
        avaliacaoFisica.setIdade(dto.getIdade());
        avaliacaoFisica.setMassaMuscular(dto.getMassaMuscular());
        avaliacaoFisica.setPeito(dto.getPeito());
        avaliacaoFisica.setPercentualGordura(dto.getPercentualGordura());
        avaliacaoFisica.setPeso(dto.getPeso());

        avaliacaoFisicaRepository.save(avaliacaoFisica);
        log.info("Avaliação física de aluno {} editada com sucesso!", avaliacaoFisica.getAluno().getEmail());

        return AvaliacaoFisicaMapper.toDTO(avaliacaoFisica);
    }

    public Page<AvaliacaoResponseDTO> buscarSuasAvaliacaoFisicaAlunos(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Aluno {} entrou em buscar suas avaliações físicas", usuario.getEmail());

        if (usuario.getRole() != RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou visualizar avaliações de alunos!", usuario.getEmail());
            throw new BusinessException("Apenas alunos podem visualizar suas avaliações físicas por aqui!");
        }

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAllByAluno(usuario, pageable);

        log.info("Aluno {} visualizou suas avaliações com sucesso!", usuario.getEmail());
        return avaliacoesFisicas
            .map(AvaliacaoFisicaMapper::toDTO);
    }

    public Page<AvaliacaoResponseDTO> buscarSuasAvaliacoesFisicasCriadas(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em buscar suas avaliações físicas criadas", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Aluno {} tenteu visualizar avaliações físicas sem permissão!", usuario.getEmail());
            throw new BusinessException("Apenas funcionários e admins podem ver suas avaliações físicas criadas!");
        }

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAllByAvaliador(
            usuario, 
            pageable
        );

        log.info("Usuário {} visualizou suas avaliações físicas criadas!", usuario.getEmail());
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
        log.info("Usuário {} entrou em buscar todas avaliações com filtro!", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou visualizar avaliações sem permissão!", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para visualizar todas as avaliações!");
        }

        Specification<AvaliacaoFisica> spec = (root, query, cb) -> null;

        spec = spec.and(AvaliacaoFisicaSpecification.aluno(aluno));
        spec = spec.and(AvaliacaoFisicaSpecification.avaliador(avaliador));
        spec = spec.and(AvaliacaoFisicaSpecification.dataAvaliacao(inicio, fim));
        spec = spec.and(AvaliacaoFisicaSpecification.idade(idade));

        Page<AvaliacaoFisica> avaliacoesFisicas = avaliacaoFisicaRepository.findAll(spec, pageable);

        log.info("Usuário {} visualizou todas avaliações com sucesso!", usuario.getEmail());
        return avaliacoesFisicas
            .map(AvaliacaoFisicaMapper::toDTO);
    }

    public AvaliacaoResponseDTO buscarAvaliacaoFisicaPorId(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em buscar avaliação física por id", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou buscar avaliação física por id sem permissão", usuario.getEmail());
            throw new BusinessException("Você não tem permissão de visualizar essa avaliação física!");
        }

        AvaliacaoFisica avaliacaoFisica = avaliacaoFisicaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Avaliação física não encontrada!"));
            
        log.info("Usuário {} visualizou avaliação física por id com sucesso!", usuario.getEmail());
        return AvaliacaoFisicaMapper.toDTO(avaliacaoFisica);       
    }

    @Transactional
    public void excluirAvaliacaoFisica(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em excluir avaliação física", usuario.getEmail());

        AvaliacaoFisica avaliacaoFisica = avaliacaoFisicaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Avaliação física não encontrada!"));

        boolean isAdmin = usuario.getRole() == RoleUser.ROLE_ADMIN;
        boolean isAvaliador = avaliacaoFisica.getAvaliador().getId().equals(usuario.getId());
    
        if (!isAdmin && !isAvaliador) {
            log.warn("Usuário {} tentou excluir avaliação física sem permissão!", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para excluir essa avaliação física!");
        }

        avaliacaoFisicaRepository.delete(avaliacaoFisica);

        log.info("Usuário {} deletou avaliação física com id: {} com sucesso!", usuario.getEmail(), avaliacaoFisica.getId());
    }

}
