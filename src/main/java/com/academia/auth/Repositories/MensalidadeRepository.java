package com.academia.auth.Repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Utils.StatusMensalidade;

public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {
    
    Optional<Mensalidade> findTopByUsuarioOrderByIdDesc(Usuario usuario);

    Long countByStatus(StatusMensalidade status);

    @Query("""
        SELECT COALESCE(SUM(m.valor), 0)
        FROM Mensalidade m
        WHERE m.status = :status
        AND m.dataPagamento BETWEEN :inicio AND :fim
    """)
    BigDecimal somarValorPorPeriodo(
        @Param("status") StatusMensalidade status, 
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );

    Page<Mensalidade> findAllByUsuario(Usuario usuario, Pageable pageable);

    Page<Mensalidade> findByUsuarioNomeContainingIgnoreCase(Pageable pageable, String nome);

    boolean existsByUsuario(Usuario usuario);

    boolean existsByUsuarioAndDataCancelamentoBetween(Usuario usuario, LocalDate inicioMes, LocalDate fimMes);

    boolean existsByUsuarioAndStatus(Usuario usuario, StatusMensalidade status);

    List<Mensalidade> findByStatusAndDataVencimentoBefore(StatusMensalidade status, LocalDate hoje);

    List<Mensalidade> findByDataCriacaoBefore(LocalDate umAnoAtras);
}
