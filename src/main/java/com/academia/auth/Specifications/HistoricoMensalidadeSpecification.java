package com.academia.auth.Specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.academia.auth.DTOS.HistoricoMensalidade.HistoricoMensalidadeFilterDTO;
import com.academia.auth.Models.HistoricoMensalidade;

import jakarta.persistence.criteria.Predicate;

public class HistoricoMensalidadeSpecification {
    
    public static Specification<HistoricoMensalidade> filtroHistoricoMensalidades(
        HistoricoMensalidadeFilterDTO filter
    ) 
    {

        return (root, query, cb) -> {
            
            List<Predicate> predicates = new ArrayList<>();

            if (filter.nomeUsuario() != null) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("nomeUsuario")),
                        "%" + filter.nomeUsuario() + "%"
                    )
                );
            };

            if (filter.diasTreino() != null) {
                predicates.add(
                    cb.equal(root.get("diasTreino"), 
                    filter.diasTreino())
                );
            };

            if (filter.dataPagamentoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("dataPagamento"),
                    filter.dataPagamentoInicio())
                );
            };

            if (filter.dataCancelamentoFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get("dataPagamento"),
                    filter.dataPagamentoFim())
                );
            };

            if (filter.dataCancelamentoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("dataCancelamento"),
                    filter.dataCancelamentoInicio())
                );
            };

            if (filter.dataCancelamentoFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get("dataCancelamento"),
                    filter.dataCancelamentoFim())
                );
            };

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
