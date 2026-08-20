package com.academia.auth.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academia.auth.Models.HistoricoMensalidade;

public interface HistoricoMensalidadeRepository extends JpaRepository<HistoricoMensalidade, Long>, 
JpaSpecificationExecutor<HistoricoMensalidade>
{
    HistoricoMensalidade findByNomeUsuario(String nome);
}
