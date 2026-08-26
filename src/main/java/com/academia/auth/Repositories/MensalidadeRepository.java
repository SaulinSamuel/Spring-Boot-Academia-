package com.academia.auth.Repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.StatusMensalidade;

public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long>,
JpaSpecificationExecutor<Mensalidade> {
    
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

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM Mensalidade m
            WHERE m.dataCriacao <= :umAno
            """)
    int excluirMensalidadesAposAno(
        @Param("umAno") LocalDate umAno
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Mensalidade m
            SET m.status = :statusAtrasada
            WHERE m.dataVencimento <= :hoje
            AND m.status = :statusPendente
            """)
    int atrasarMensalidades(
        @Param("statusAtrasada") StatusMensalidade statusAtrasada,
        @Param("statusPendente") StatusMensalidade statusPendente,
        @Param("hoje") LocalDate hoje
    );

    Page<Mensalidade> findAllByUsuario(Usuario usuario, Pageable pageable);

    Page<Mensalidade> findByUsuarioNomeContainingIgnoreCase(Pageable pageable, String nome);

    boolean existsByUsuario(Usuario usuario);

    boolean existsByUsuarioAndDataCancelamentoBetween(Usuario usuario, LocalDate inicio, LocalDate fim);

    boolean existsByUsuarioAndStatus(Usuario usuario, StatusMensalidade status);
}
