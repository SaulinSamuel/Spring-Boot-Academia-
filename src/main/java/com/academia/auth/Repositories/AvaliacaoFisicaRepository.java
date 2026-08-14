package com.academia.auth.Repositories;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academia.auth.Models.AvaliacaoFisica;
import com.academia.auth.Models.Usuario;

public interface AvaliacaoFisicaRepository extends JpaRepository<AvaliacaoFisica, Long>,
JpaSpecificationExecutor<AvaliacaoFisica>
{

    boolean existsByAlunoAndDataAvaliacaoBetween(Usuario usuario, LocalDate inicio, LocalDate fim);

    Page<AvaliacaoFisica> findAllByAvaliador(Usuario usuario, Pageable pageable);

    Page<AvaliacaoFisica> findAllByAluno(Usuario usuario, Pageable pageable);
}
