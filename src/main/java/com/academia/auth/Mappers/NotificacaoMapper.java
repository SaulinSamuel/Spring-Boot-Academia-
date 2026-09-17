package com.academia.auth.Mappers;

import org.springframework.stereotype.Component;

import com.academia.auth.DTOS.Notificacao.NotificacaoResponseDTO;
import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.TipoNotificacao;

@Component 
public class NotificacaoMapper {
    
    public Notificacao toEntity(
        Usuario usuario,
        TipoNotificacao tipoNotificacao,
        String mensagem,
        String titulo
    ) 
    {

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setMensagem(mensagem);
        notificacao.setTipoNotificacao(tipoNotificacao);
        notificacao.setTitulo(titulo);

        return notificacao;
    }

    public NotificacaoResponseDTO toDTO(Notificacao notificacao) {

        NotificacaoResponseDTO dto = new NotificacaoResponseDTO(
            notificacao.getId(),
            notificacao.getTitulo(),
            notificacao.getMensagem(),
            notificacao.getTipoNotificacao()
        );

        return dto;
    }

}
