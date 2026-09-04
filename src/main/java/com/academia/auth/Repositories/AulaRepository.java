package com.academia.auth.Repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Aula;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.StatusAula;

public interface AulaRepository extends JpaRepository<Aula, Long>,
JpaSpecificationExecutor<Aula>
{
    
    Optional<Aula> findTopByInstrutorOrderByIdDesc(Usuario usuario);

    Page<Aula> findAllByInstrutor(Usuario instrutor, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Aula a
            SET a.status = :statusNovo
            WHERE a.status = :status
            AND FUNCTION('TIMESTAMP', a.dataAula, a.horarioInicio) <= :tempoLimite
            """)
    int cancelarAulas(
        @Param("status") StatusAula status,
        @Param("statusNovo") StatusAula statusNovo,
        @Param("tempoLimite") LocalDateTime tempoLimite
    );  

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Aula a
            SET a.status = :statusNovo
            WHERE a.status = :status
            AND FUNCTION('TIMESTAMP', a.dataAula, a.horarioFim) <= :agora
            """)
    int concluirAulas(
        @Param("statusNovo") StatusAula statusNovo,
        @Param("status") StatusAula status,
        @Param("agora") LocalDateTime agora
    );

}
