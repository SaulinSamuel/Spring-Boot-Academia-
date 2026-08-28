package com.academia.auth.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.auth.Models.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
}
