package com.academia.auth.Services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Mensalidade.MensalidadeFilterDatesDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Events.MensalidadeCriadaEvent;
import com.academia.auth.Events.MensalidadeStatusAlteradoEvent;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.MensalidadeMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Models.enums.StatusMensalidade;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;
import com.academia.auth.Specifications.MensalidadeSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MensalidadeService {
    
    private final MensalidadeRepository mensalidadeRepository;
    private final UsuarioAutenticadoService usuarioLogado;
    private final AcessoAcademiaRepository academiaRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public MensalidadeResponseDTO criarMensalidade(MensalidadeRequestDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        log.info("Criando mensalidade para usuário {}", usuario.getEmail());

        if(mensalidadeRepository.existsByUsuarioAndStatus(usuario, StatusMensalidade.PENDENTE)) {
            log.warn("Usuário {} tentou criar mensalidade mas já possui pendentes!", usuario.getEmail());
            throw new BusinessException("Você já possui mensalidades pendentes!");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(inicioMes.lengthOfMonth());

        if (mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween(usuario, inicioMes, fimMes)) {
            log.warn("Usuário {} tentou cancelar mais de uma mensalidade no mês.", usuario.getEmail());
            throw new BusinessException("Você já cancelou uma mensalidade esse mês!");
        }

        BigDecimal valor = validarValorMensalidade(dto.getDiasTreino());

        Mensalidade mensalidade = MensalidadeMapper.toEntity(dto);

        applicationEventPublisher.publishEvent(
            new MensalidadeCriadaEvent(usuario)
        );

        mensalidade.setValor(valor);
        mensalidade.setDataCriacao(hoje);
        mensalidade.setDataVencimento(hoje.plusMonths(1));
        mensalidade.setUsuario(usuario);
        mensalidade.setDiasTreino(dto.getDiasTreino());
        mensalidade.setDataPagamento(null);
        mensalidade.setDataCancelamento(null);
        mensalidade.setAtualizacoes(0);
        mensalidade.setStatus(StatusMensalidade.PENDENTE);

        mensalidadeRepository.save(mensalidade);

        log.info("Mensalidade criada e salva para usuário {}", usuario.getEmail());
        log.info("Acesso da academia criado e salvo para usuário {}", usuario.getEmail());

        return MensalidadeMapper.toDTO(mensalidade);
    }

    @Transactional
    public MensalidadeResponseDTO atualizarMensalidade(MensalidadeRequestDTO dto) {
        
        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em atualizar mensalidade", usuario.getEmail());

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            throw new BusinessException("Apenas mensalidades pendentes podem ser alteradas!");
        }

        if (mensalidade.getAtualizacoes() >= 1) {
            log.info("Usuário {} tentou atualizar mensalidade mais de uma vez no mês", usuario.getEmail());
            throw new BusinessException("Você só pode atualizar sua mensalidade 1 vez por mês!");
        }

        BigDecimal valor = validarValorMensalidade(dto.getDiasTreino());

        mensalidade.setDiasTreino(dto.getDiasTreino());
        mensalidade.setValor(valor);
        mensalidade.setAtualizacoes(1);

        mensalidadeRepository.save(mensalidade);
        log.info("Mensalidade do usuário {} atualizada!", usuario.getEmail());

        return MensalidadeMapper.toDTO(mensalidade);
    }

    public Page<MensalidadeResponseDTO> buscarSuasMensalidades(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} tentou buscar suas mensalidades", usuario.getEmail());

        Page<Mensalidade> mensalidades = mensalidadeRepository.findAllByUsuario(usuario, pageable);

        log.info("Usuário {} buscou suas mensalidades", usuario.getEmail());

        return mensalidades
            .map(MensalidadeMapper::toDTO);
    }

    public Page<MensalidadeResponseDTO> buscarTodasMensalidadesComFiltro(
        Integer diasTreino,
        MensalidadeFilterDatesDTO filterDatesDTO,    
        Pageable pageable) 
    {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em buscar todas as mensalidades", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou entrar sem permissão em método buscarTodasMensalidades", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para visualizar as mensalidades!");
        }

        Specification<Mensalidade> specification = MensalidadeSpecification.filterDates(filterDatesDTO);
        specification = specification.and(MensalidadeSpecification.diasTreino(diasTreino));

        Page<Mensalidade> mensalidades = mensalidadeRepository.findAll(specification, pageable);

        log.info("Usuário {} buscou todas mensalidades", usuario.getEmail());
        return mensalidades
            .map(MensalidadeMapper::toDTO);
    }

    public Page<MensalidadeResponseDTO> buscarMensalidadesPorNome(Pageable pageable, String nome) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em buscar mensalidades por nome", usuario.getEmail());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou pesquisar nomes sem permissão", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para visualizar outras mensalidades!");
        }

        Page<Mensalidade> mensalidades = mensalidadeRepository.findByUsuarioNomeContainingIgnoreCase(pageable, nome);

        log.info("Usuário {} pesquisou mensalidades!", usuario.getEmail());
        return mensalidades
            .map(MensalidadeMapper::toDTO);
    }

    @Transactional
    public MensalidadeResponseDTO pagarMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em pagar mensalidade", usuario.getEmail());

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));
        
        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE && 
            mensalidade.getStatus() != StatusMensalidade.ATRASADA) {
            log.warn("Usuário {} tentou pagar mensalidade {} já paga ou cancelada!", usuario.getEmail(), mensalidade.getId());
            throw new BusinessException("Apenas mensalidades pendentes(ou atrasadas) podem ser pagas!");
        }

        mensalidade.setStatus(StatusMensalidade.PAGA);
        mensalidade.setDataPagamento(LocalDate.now());
        
        mensalidadeRepository.save(mensalidade);

        applicationEventPublisher.publishEvent(
            new MensalidadeStatusAlteradoEvent(mensalidade)
        );

        log.info("Mensalidade {} salva e paga", mensalidade.getId());
        
        gerarProximaMensalidade(mensalidade);

        return MensalidadeMapper.toDTO(mensalidade);
    }

    @Transactional
    public MensalidadeResponseDTO cancelarMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em cancelar mensalidade", usuario.getEmail());

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            log.warn("Usuário {} tentou cancelar mensalidade não pendente!", usuario.getEmail());
            throw new BusinessException("Apenas mensalidades pendentes podem ser canceladas!");
        }

        mensalidade.setStatus(StatusMensalidade.CANCELADA);
        mensalidade.setDataCancelamento(LocalDate.now());     

        mensalidadeRepository.save(mensalidade);

        log.info("Mensalidade {} cancelada", mensalidade.getId());

        if (usuario.getRole() == RoleUser.ROLE_USER) {
            AcessoAcademia acessoAcademia = academiaRepository.findByUsuario(usuario)
            .orElseThrow(() -> new ResourceNotFound("Acesso academia não encontrado!"));

            usuario.setAcessosAcademia(null);

            academiaRepository.delete(acessoAcademia);
            log.info("Acesso {} deletado", acessoAcademia.getId());
        }
        
        applicationEventPublisher.publishEvent(
            new MensalidadeStatusAlteradoEvent(mensalidade)
        );
        
        return MensalidadeMapper.toDTO(mensalidade);
    }

    @Transactional
    public void excluirMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em excluir mensalidade", usuario.getEmail());

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PAGA &&
        mensalidade.getStatus() != StatusMensalidade.CANCELADA) {

            log.warn("Usuário {} tentou cancelar mensalidade {} atrasada ou pendente", usuario.getEmail(), mensalidade.getId());
            throw new BusinessException("Apenas mensalidades pagas(ou canceladas) podem ser excluídas!");    
        }

        mensalidadeRepository.delete(mensalidade);

        log.info("Mensalidade {} excluída", mensalidade.getId());
    }

    @Transactional
    public MensalidadeResponseDTO gerarProximaMensalidade(Mensalidade mensalidade) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Nova mensalidade de usuário {} sendo gerada", usuario.getEmail());

        if (mensalidade.getStatus() != StatusMensalidade.PAGA) {
            log.warn("Mensalidade de usuário {} ainda não foi paga", usuario.getEmail());
            throw new BusinessException("Mensalidade ainda não paga!");
        }

        Mensalidade mensalidadeNova = new Mensalidade();

        mensalidadeNova.setDataCriacao(LocalDate.now());
        mensalidadeNova.setDataPagamento(null);
        mensalidadeNova.setDataVencimento(mensalidade.getDataVencimento().plusMonths(1));
        mensalidadeNova.setDataCancelamento(null);
        mensalidadeNova.setDiasTreino(mensalidade.getDiasTreino());
        mensalidadeNova.setStatus(StatusMensalidade.PENDENTE);
        mensalidadeNova.setValor(mensalidade.getValor());
        mensalidadeNova.setUsuario(usuario);
        mensalidadeNova.setAtualizacoes(0);

        mensalidadeRepository.save(mensalidadeNova);
        log.info("Nova mensalidade após pagamento de usuário {} criada", usuario.getEmail());
        
        return MensalidadeMapper.toDTO(mensalidadeNova); 
    }

    private BigDecimal validarValorMensalidade(Integer diasTreino) {
        
        BigDecimal valor;
        
        if (diasTreino >= 1 && diasTreino <= 5) {
            valor = BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(diasTreino));
        } else {
            throw new BusinessException("Máximo de dias de treino são 5!");
        }

        return valor;
    }

}
