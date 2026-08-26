package com.academia.auth.Events;

import com.academia.auth.Models.Usuario;

public record UsuarioRebaixadoEvent(
    Usuario usuario
) {
    
}
