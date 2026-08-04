package com.academia.auth.Repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.Usuario;

import jakarta.transaction.Transactional;

public interface AdvertenciaRepository extends JpaRepository<Advertencia, Long> {
    
    Long countByDestinatario(Usuario usuario);

    List<Advertencia> findAllByDestinatario(Usuario usuario);

    Page<Advertencia> findAllByDestinatario(Usuario usuario, Pageable pageable);

    Page<Advertencia> findAllByRemetente(Usuario usuario, Pageable pageable);

    Page<Advertencia> findByDestinatarioNomeContainingIgnoreCase(String nome, Pageable pageable);  

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Advertencia a
            WHERE a.dataExpiracao <= :hoje
            """)
    int excluirAdvertencias(@Param("hoje") LocalDateTime hoje);
}
