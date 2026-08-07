package com.academia.auth.Specifications;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.Models.HistoricoAcessos;

public class HistoricoAcessosSpecification {
    
    public static Specification<HistoricoAcessos> nomeUsuario(String nomeUsuario) {

        return (root, query, cb) -> {

            if (nomeUsuario == null || nomeUsuario.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("nomeUsuario")),
                "%" + nomeUsuario.toLowerCase() + "%"
            );
        };
    }

    public static Specification<HistoricoAcessos> horarioEntrada(
        LocalDateTime inicio, 
        LocalDateTime fim
    )
    {

        return (root, query, cb) -> {

            if (inicio == null || fim == null) {
                return null;
            }

            return cb.between(
                root.get("horarioEntrada"), inicio, fim
            );
        };
    }

    public static Specification<HistoricoAcessos> diaDaSemana(DayOfWeek diaDaSemana) {

        return (root, query, cb) -> {

            if (diaDaSemana == null) {
                return null;
            }

            return cb.equal(
                root.get("diaDaSemana"), diaDaSemana
            );
        };
    }

}
