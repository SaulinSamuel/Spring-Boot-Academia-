package com.academia.auth.DTOS.Notificacao;

import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.enums.TipoNotificacao;

public record NotificacaoResponseDTO(
    
    Long id,

    String titulo,

    String mensagem,

    TipoNotificacao tipoNotificacao,

    boolean lida

) 
{
    
    public static NotificacaoResponseDTO from(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
            notificacao.getId(), 
            notificacao.getTitulo(), 
            notificacao.getMensagem(), 
            notificacao.getTipoNotificacao(),
            notificacao.isLida()      
        );
    }
}
