package com.academia.auth.Specifications;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.Models.Advertencia;
import com.academia.auth.Models.enums.AdvertenciaStatus;

public class AdvertenciaSpecification {
    
    public static Specification<Advertencia> remetente(String remetente) {

        return (root, query, cb) -> {

            if (remetente == null || remetente.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("remetente").get("name")),
                "%" + remetente.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Advertencia> destinatario(String destinatario) {
        return (root, query, cb) -> {

            if (destinatario == null || destinatario.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("destinatario").get("name")),
                "%" + destinatario.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Advertencia> nivelAdvertencia(AdvertenciaStatus nivelAdvertencia) {

        return (root, query, cb) -> {

            if (nivelAdvertencia == null) {
                return null;
            }

            return cb.equal(
                root.get("nivelAdvertencia"), nivelAdvertencia
            );
        };
    }

    public static Specification<Advertencia> dataCriacao(
        LocalDateTime inicio,
        LocalDateTime fim
    ) 
    {

        return (root, query, cb) -> {

            if (inicio == null || fim == null) {
                return null;
            }

            return cb.between(
                root.get("dataCriacao"), inicio, fim
            );
        };
    }

}
