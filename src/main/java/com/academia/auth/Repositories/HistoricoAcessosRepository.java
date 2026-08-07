package com.academia.auth.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academia.auth.Models.HistoricoAcessos;

public interface HistoricoAcessosRepository extends JpaRepository<HistoricoAcessos, Long>,
JpaSpecificationExecutor<HistoricoAcessos>
{
    
}
