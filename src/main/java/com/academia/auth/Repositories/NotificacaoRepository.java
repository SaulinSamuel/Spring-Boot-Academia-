package com.academia.auth.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Notificacao;
import com.academia.auth.Models.Usuario;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    
    Page<Notificacao> findAllByUsuario(Usuario usuario, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Notificacao n
            SET n.lida = true
            WHERE n.usuario.id = :usuarioId
            """)
    int marcarTodasNotificacoesComoLida(@Param("usuarioId") Long usuarioId);

}
