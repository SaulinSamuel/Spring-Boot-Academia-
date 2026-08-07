package com.academia.auth.Repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.academia.auth.Models.AcessoAcademia;
import com.academia.auth.Models.Usuario;

public interface AcessoAcademiaRepository extends JpaRepository<AcessoAcademia, Long> {
    
    Optional<AcessoAcademia> findByUsuario(Usuario usuario);

    @Query("""
            SELECT COALESCE(SUM(a.diasAcesso), 0)
            FROM AcessoAcademia a   
            """)
    Long somarDiasAcessadosSemana();

    Page<AcessoAcademia> findByNomeContainingIgnoreCase(Pageable pageable, String nome);
}
