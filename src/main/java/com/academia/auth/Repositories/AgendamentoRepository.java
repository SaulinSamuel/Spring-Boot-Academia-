package com.academia.auth.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academia.auth.Models.Agendamento;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.StatusAula;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
            SELECT COUNT(a.id) > 0
            FROM Agendamento     a
            JOIN a.aula au          
            WHERE a.usuario.id = :usuarioId
                AND au.id <> :aulaId    
                AND au.status = :statusAula  
            """)
    boolean existeMaisDeUmAgendamentoComStatus(       
        @Param("usuarioId") Long usuarioId,
        @Param("aulaId") Long aulaId,
        @Param("statusAula") StatusAula statusAula
    );

    Page<Agendamento> findAllByUsuario(Usuario usuario, Pageable pageable);

}
