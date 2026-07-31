package com.academia.auth.Repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);

    Long countByRole(RoleUser role);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Usuario> findAll(Pageable pageable);
}
