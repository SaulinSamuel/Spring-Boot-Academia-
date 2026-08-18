package com.academia.auth.Specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.Mensalidade.MensalidadeFilterDatesDTO;
import com.academia.auth.Models.Mensalidade;

import jakarta.persistence.criteria.Predicate;

public class MensalidadeSpecification {
    
    public static Specification<Mensalidade> diasTreino(Integer diasTreino) {

        return (root, query, cb) -> {

            if (diasTreino == null) {

                return null;
            }

            return cb.equal(
                root.get("diasTreino"), diasTreino
            );
        };
    }

    public static Specification<Mensalidade> filterDates(
        MensalidadeFilterDatesDTO filter) 
    {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.dataCancelamentoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("dataCancelamento"),
                        filter.dataCancelamentoInicio()
                    )
                );
            }

            if (filter.dataCancelamentoFim() != null) { 
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("dataCancelamento"),
                        filter.dataCancelamentoFim()
                    )
                );
            } 

            if (filter.dataCriacaoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("dataCriacao"),
                        filter.dataCriacaoInicio()
                    )
                );
            }

            if (filter.dataCriacaoFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("dataCriacao"),
                        filter.dataCriacaoFim()
                    )
                );
            }

            if (filter.dataPagamentoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("dataPagamento"),
                        filter.dataPagamentoInicio()
                    )
                );
            }

            if (filter.dataPagamentoFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("dataPagamento"),
                        filter.dataPagamentoFim()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
