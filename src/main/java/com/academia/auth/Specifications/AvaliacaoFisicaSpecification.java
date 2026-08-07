package com.academia.auth.Specifications;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.Models.AvaliacaoFisica;

public class AvaliacaoFisicaSpecification {
    
    public static Specification<AvaliacaoFisica> aluno(String aluno) {

        return (root, query, cb) -> {

            if (aluno == null || aluno.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("aluno").get("nome")),
                "%" + aluno.toLowerCase() + "%"
            );
        };
    }

    public static Specification<AvaliacaoFisica> avaliador(String avaliador) {

        return (root, query, cb) -> {

            if (avaliador == null || avaliador.isBlank()) {
                return null;
            }

            return cb.like(
                cb.lower(root.get("avaliador").get("nome")),
                "%" + avaliador.toLowerCase() + "%"
            );
        };
    }

    public static Specification<AvaliacaoFisica> idade(Integer idade) {

        return (root, query, cb) -> {

            if (idade == null) {
                return null;
            }

            return cb.equal(
                root.get("idade"), idade
            );
        };
    }

    public static Specification<AvaliacaoFisica> dataAvaliacao(
        LocalDate inicio,
        LocalDate fim
    )
    {

        return (root, query, cb) -> {

            if (inicio == null || fim == null) {
                return null;
            }

            return cb.between(
                root.get("dataAvaliacao"), inicio, fim
            );
        };
    }

}
