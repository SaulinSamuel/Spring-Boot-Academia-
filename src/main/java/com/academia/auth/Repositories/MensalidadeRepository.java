package com.academia.auth.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Utils.StatusMensalidade;

public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {
    
    Optional<Mensalidade> findTopByUsuarioOrderByIdDesc(Usuario usuario);
    Page<Mensalidade> findAllByUsuario(Usuario usuario, Pageable pageable);
    boolean existsByUsuario(Usuario usuario);
    boolean existsByStatus(StatusMensalidade status);
    List<Mensalidade> findByStatusAndDataVencimentoBefore(StatusMensalidade status, LocalDateTime hoje);
}
