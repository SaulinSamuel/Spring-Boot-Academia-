package com.academia.auth.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    
    Page<Notificacao> findAllByUsuario(Usuario usuario, Pageable pageable);

}
