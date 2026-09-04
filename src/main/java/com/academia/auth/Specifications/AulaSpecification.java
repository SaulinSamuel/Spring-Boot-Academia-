package com.academia.auth.Specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.Aula.AulaFilterDTO;
import com.academia.auth.Models.Aula;
import com.academia.auth.Models.enums.StatusAula;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public class AulaSpecification {
    
    public static Specification<Aula> filter(AulaFilterDTO filter) {

        return(root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.nome() != null) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("nome")),
                        "%" + filter.nome().toLowerCase() + "%"
                    )
                );
            };

            if (filter.nomeInstrutor() != null) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("instrutor").get("nome")),
                        "%" + filter.nomeInstrutor().toLowerCase() + "%"
                    )
                );
            };

            if (filter.dataAulaInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("dataAula"),
                    filter.dataAulaInicio())
                );
            };

            if (filter.dataAulaFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get("dataAula"),
                    filter.dataAulaFim())
                );
            };

            if (filter.capacidadeInscricoes() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get("capacidadeInscricoes"),
                    filter.capacidadeInscricoes())
                );
            };

            Expression<Integer> relevanciaAulas = 
                cb.<Integer>selectCase()
                    .when(
                        cb.equal(root.get("status"), StatusAula.PENDENTE),
                        0
                    )
                    .otherwise(1);

            query.orderBy(
                cb.asc(relevanciaAulas),
                cb.asc(root.get("dataAula"))
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
