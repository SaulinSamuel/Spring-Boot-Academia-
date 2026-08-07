package com.academia.auth.Services.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Usuario;

@Service
public class UsuarioAutenticadoService {
    
    public Usuario usuarioLogado() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || 
            !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new BusinessException("Usuário não autenticado!");
        }

        return usuario;
    }
}
