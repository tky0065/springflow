package io.springflow.core.repository;

import io.springflow.core.dto.SearchRequest;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpecificationBuilder {

    public static <T> Specification<T> build(SearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (SearchRequest.FilterCriteria criteria : request.criteria()) {
                Path<?> path = root.get(criteria.field());
                Object value = criteria.value();

                Predicate predicate = switch (criteria.operator()) {
                    case EQUALS -> cb.equal(path, value);
                    case NOT_EQUALS -> cb.notEqual(path, value);
                    case GREATER_THAN -> cb.greaterThan(path.as(Comparable.class), (Comparable) value);
                    case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN -> cb.lessThan(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case LIKE -> cb.like(path.as(String.class), "%" + value + "%");
                    case IN -> path.in((Collection<?>) value);
                    case IS_NULL -> cb.isNull(path);
                    case IS_NOT_NULL -> cb.isNotNull(path);
                };
                predicates.add(predicate);
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            if (request.operator() == SearchRequest.LogicalOperator.OR) {
                return cb.or(predicates.toArray(new Predicate[0]));
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
