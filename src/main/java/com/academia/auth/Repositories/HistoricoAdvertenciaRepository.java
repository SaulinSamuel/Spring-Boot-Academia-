package com.academia.auth.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academia.auth.Models.HistoricoAdvertencia;

public interface HistoricoAdvertenciaRepository extends JpaRepository<HistoricoAdvertencia, Long>,
JpaSpecificationExecutor<HistoricoAdvertencia>
{
    
    Page<HistoricoAdvertencia> findByRemetenteContainingIgnoreCase(String nome, Pageable pageable);

}
