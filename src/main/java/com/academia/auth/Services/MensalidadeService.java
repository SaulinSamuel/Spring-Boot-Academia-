package com.academia.auth.Services;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.academia.auth.DTOS.Mensalidade.MensalidadeRequestDTO;
import com.academia.auth.DTOS.Mensalidade.MensalidadeResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.MensalidadeMapper;
import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.AcessoAcademiaRepository;
import com.academia.auth.Repositories.MensalidadeRepository;
import com.academia.auth.Utils.StatusMensalidade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@Service
public class MensalidadeService {
    
    private final MensalidadeRepository mensalidadeRepository;
    private final UsuarioAutenticadoService usuarioLogado;
    private final AcessoAcademiaRepository academiaRepository;

    @Transactional
    public MensalidadeResponseDTO criarMensalidade(MensalidadeRequestDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if(mensalidadeRepository.existsByStatus(StatusMensalidade.PENDENTE)) {
            throw new BusinessException("Você já possui mensalidades pendentes!");
        }

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        if (mensalidadeRepository.existsByUsuarioAndDataCancelamentoBetween (usuario, inicioMes, fimMes)) {
            throw new BusinessException("Você já cancelou uma mensalidade esse mês!");
        }

        BigDecimal valor;

        if (dto.getDiasTreino() >= 1 && dto.getDiasTreino() <= 5) {
            valor = BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(dto.getDiasTreino()));
        } else {
            throw new BusinessException("Máximo de dias de treino são 5!");
        }

        Mensalidade mensalidade = MensalidadeMapper.toEntity(dto);

        AcessoAcademia acessosAcademia = new AcessoAcademia();
        acessosAcademia.setUsuario(usuario);
        acessosAcademia.setInicioSemana(LocalDate.now().with(DayOfWeek.MONDAY));
        acessosAcademia.setDiasAcesso(0);

        mensalidade.setValor(valor);
        mensalidade.setDataCriacao(LocalDate.now());
        mensalidade.setDataVencimento(LocalDate.now().plusMonths(1));
        mensalidade.setUsuario(usuario);
        mensalidade.setDiasTreino(dto.getDiasTreino());
        mensalidade.setDataPagamento(null);
        mensalidade.setAtualizacoes(0);
        mensalidade.setStatus(StatusMensalidade.PENDENTE);

        mensalidadeRepository.save(mensalidade);
        academiaRepository.save(acessosAcademia);

        return MensalidadeMapper.toDTO(mensalidade);
    }

    @Transactional
    public MensalidadeResponseDTO atualizarMensalidade(MensalidadeRequestDTO dto) {
        
        Usuario usuario = usuarioLogado.usuarioLogado();

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            throw new BusinessException("Apenas mensalidades pendentes podem ser alteradas!");
        }

        if (mensalidade.getAtualizacoes() >= 1) {
            throw new BusinessException("Você só pode atualizar sua mensalidade 1 vez por mês!");
        }

        BigDecimal valor = validarValorMensalidade(dto.getDiasTreino());

        mensalidade.setDiasTreino(dto.getDiasTreino());
        mensalidade.setValor(valor);
        mensalidade.setAtualizacoes(1);

        mensalidadeRepository.save(mensalidade);

        return MensalidadeMapper.toDTO(mensalidade);
    }

    public Page<MensalidadeResponseDTO> buscarSuasMensalidades(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Page<Mensalidade> mensalidades = mensalidadeRepository.findAllByUsuario(usuario, pageable);

        if (mensalidades.isEmpty()) {
            throw new ResourceNotFound("Mensalidades não encontradas!");
        }

        return mensalidades
            .map(MensalidadeMapper::toDTO);
    }

    public Page<MensalidadeResponseDTO> buscarTodasMensalidades(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (usuario.getRole() != RoleUser.ROLE_ADMIN) {
            throw new BusinessException("Você não tem permissão para visualizar as mensalidades!");
        }

        Page<Mensalidade> mensalidades = mensalidadeRepository.findAll(pageable);

        if (mensalidades.isEmpty()) {
            throw new ResourceNotFound("Mensalidades não encontradas!");
        }

        return mensalidades
            .map(MensalidadeMapper::toDTO);
    }

    @Transactional
    public MensalidadeResponseDTO pagarMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));
        
        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE && 
            mensalidade.getStatus() != StatusMensalidade.ATRASADA) {
            throw new BusinessException("Apenas mensalidades pendentes(ou atrasadas) podem ser pagas!");
        }

        mensalidade.setStatus(StatusMensalidade.PAGA);
        mensalidade.setDataPagamento(LocalDate.now());
        mensalidade.setAtualizacoes(0);

        mensalidadeRepository.save(mensalidade);
        
        gerarProximaMensalidade(mensalidade);

        return MensalidadeMapper.toDTO(mensalidade);
    }

    @Transactional
    public void atrasarMensalidades() {

        LocalDate hoje = LocalDate.now();

        List<Mensalidade> mensalidadesVencidas = mensalidadeRepository
            .findByStatusAndDataVencimentoBefore(StatusMensalidade.PENDENTE, hoje);

        for (Mensalidade mensalidade : mensalidadesVencidas) {
            mensalidade.setStatus(StatusMensalidade.ATRASADA);
        }

        mensalidadeRepository.saveAll(mensalidadesVencidas);
    }

    @Transactional
    public MensalidadeResponseDTO cancelarMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PENDENTE) {
            throw new BusinessException("Apenas mensalidades pendentes podem ser canceladas!");
        }

        mensalidade.setStatus(StatusMensalidade.CANCELADA);
        mensalidade.setDataCancelamento(LocalDate.now());

        mensalidadeRepository.save(mensalidade);

        return MensalidadeMapper.toDTO(mensalidade);
    }

    public void excluirMensalidade() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        Mensalidade mensalidade = mensalidadeRepository.findTopByUsuarioOrderByIdDesc(usuario)
            .orElseThrow(() -> new ResourceNotFound("Mensalidade não encontrada!"));

        if (mensalidade.getStatus() != StatusMensalidade.PAGA &&
        mensalidade.getStatus() != StatusMensalidade.CANCELADA) {
            throw new BusinessException("Apenas mensalidades pagas(ou canceladas) podem ser excluídas!");    
        }

        mensalidadeRepository.delete(mensalidade);
    }

    @Transactional
    public MensalidadeResponseDTO gerarProximaMensalidade(Mensalidade mensalidade) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if (mensalidade.getStatus() != StatusMensalidade.PAGA) {
            throw new BusinessException("Mensalidade ainda não paga!");
        }

        Mensalidade mensalidadeNova = new Mensalidade();

        mensalidadeNova.setDataCriacao(LocalDate.now());
        mensalidadeNova.setDataPagamento(null);
        mensalidadeNova.setDataVencimento(mensalidade.getDataVencimento().plusMonths(1));
        mensalidadeNova.setDiasTreino(mensalidade.getDiasTreino());
        mensalidadeNova.setStatus(StatusMensalidade.PENDENTE);
        mensalidadeNova.setValor(mensalidade.getValor());
        mensalidadeNova.setUsuario(usuario);

        mensalidadeRepository.save(mensalidadeNova);
        
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
