package com.academia.auth.Services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Notificacao.NotificacaoResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.NotificacaoMapper;
import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;
import com.academia.auth.Repositories.NotificacaoRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RequiredArgsConstructor 
@Service 
public class NotificacaoService {
    
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioAutenticadoService usuarioLogado;
    private final NotificacaoMapper notificacaoMapper;

    @Transactional
    public void enviarNotificacao(
        TipoNotificacao tipoNotificacao,
        String titulo,
        String mensagem,
        List<Usuario> usuarios
    )
    {

        List<Notificacao> notificacoes = usuarios.stream()
            .map(usuario -> notificacaoMapper.toEntity(
                usuario, 
                tipoNotificacao, 
                mensagem,
                titulo
            ))
        .toList();

        notificacaoRepository.saveAll(notificacoes);
    }

    @Transactional
    public NotificacaoResponseDTO marcarNotificacaoComoLida(Long notificacaoId) {

        var usuario = usuarioLogado.usuarioLogado();

        var notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new ResourceNotFound("Notificação não encontrada!"));

        if (!notificacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Você não tem permissão de ler esta notificação!");
        }
        
        notificacao.setLida(true);

        notificacaoRepository.save(notificacao);

        return notificacaoMapper.toDTO(notificacao);
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponseDTO> buscarMinhasNotificacoes(Pageable pageable) {

        var usuario = usuarioLogado.usuarioLogado();

        Page<Notificacao> notificacoes = notificacaoRepository.findAllByUsuario(usuario, pageable);

        return notificacoes
            .map(notificacaoMapper::toDTO);
    }

    @Transactional 
    public NotificacaoResponseDTO buscarNotificacaoPorId(Long notificacaoId) {

        var usuario = usuarioLogado.usuarioLogado();

        var notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new ResourceNotFound("Notificação não encontrada!"));  
        
        if (!notificacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Você não tem permissão de visualizar essa notificação!");
        }

        return notificacaoMapper.toDTO(notificacao);
    }

    @Transactional 
    public void deletarNotificacao(Long notificacaoId) {

        var usuario = usuarioLogado.usuarioLogado();

        var notificacao = notificacaoRepository.findById(notificacaoId)
            .orElseThrow(() -> new ResourceNotFound("Notificação não encontrada!"));

        if (!notificacao.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Você não tem permissão de deletar essa notificação!");
        }

        notificacaoRepository.delete(notificacao);
    }

}
