package com.academia.auth.Repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academia.auth.Models.Mensalidade;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Utils.StatusMensalidade;

public interface MensalidadeRepository extends JpaRepository<Mensalidade, Long> {
    
    Optional<Mensalidade> findTopByUsuarioOrderByIdDesc(Usuario usuario);

    Page<Mensalidade> findAllByUsuario(Usuario usuario, Pageable pageable);

    boolean existsByUsuario(Usuario usuario);

    boolean existsByUsuarioAndDataCancelamentoBetween(Usuario usuario, LocalDate inicioMes, LocalDate fimMes);

    boolean existsByUsuarioAndStatus(Usuario usuario, StatusMensalidade status);

    List<Mensalidade> findByStatusAndDataVencimentoBefore(StatusMensalidade status, LocalDate hoje);

    List<Mensalidade> findByDataCriacaoBefore(LocalDate umAnoAtras);
}
