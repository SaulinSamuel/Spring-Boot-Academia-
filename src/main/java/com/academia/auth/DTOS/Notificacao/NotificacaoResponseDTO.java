package com.academia.auth.DTOS.Notificacao;

import com.academia.auth.Models.enums.TipoNotificacao;

public record NotificacaoResponseDTO(
    
    Long id,

    String titulo,

    String mensagem,

    TipoNotificacao tipoNotificacao,

    boolean lida

) {
    
}
