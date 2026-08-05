package com.academia.auth.Specifications;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.Models.HistoricoAdvertencia;
import com.academia.auth.Models.enums.AdvertenciaStatus;

public class HistoricoAdvertenciaSpecification {
    
    public static Specification<HistoricoAdvertencia> destinatario(String destinatario) {

        return (root, query, cb) -> {

            if (destinatario == null || destinatario.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("destinatario")),
                "%" + destinatario.toLowerCase() + "%"
            );
        };  
    }

    public static Specification<HistoricoAdvertencia> remetente(String remetente) {

        return (root, query, cb) -> {

            if (remetente == null || remetente.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("remetente")),
                "%" + remetente.toLowerCase() + "%"
            );
        };
    }

    public static Specification<HistoricoAdvertencia> excluidoPor(String excluidoPor) {

        return (root, query, cb) -> {

            if (excluidoPor == null) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("excluidoPor")),
                "%" + excluidoPor.toLowerCase() + "%"
            );
        };
    }

    public static Specification<HistoricoAdvertencia> dataExclusão(
        LocalDateTime inicio,
        LocalDateTime fim) 
    {

        return (root, query, cb) -> {

            if (inicio == null || fim == null) {
                return null;
            }

            return cb.between(
                root.get("dataExclusao"),
                inicio,
                fim
            );
        };
    }

    public static Specification<HistoricoAdvertencia> nivelAdvertencia(AdvertenciaStatus nivelAdvertencia) {

        return (root, query, cb) -> {

            if (nivelAdvertencia == null) {
                return null;
            }

            return cb.equal(
                root.get("nivelAdvertencia"), nivelAdvertencia
            );
        };
    }

}
