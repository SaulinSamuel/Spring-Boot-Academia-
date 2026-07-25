package com.academia.auth.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.academia.auth.Repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UsuarioRepository usuarioRepository;

    public UserDetails loadUserByUsername(String email) {
        
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new 
                UsernameNotFoundException("Usuário não encontrado!")
            ); 
    }

}
