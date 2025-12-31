package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/**
 * Generic JPA Specification for dynamic filtering.
 *
 * @param <T> Entity type
 * @author SpringFlow
 */
public class GenericSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        FilterType operation = criteria.getOperation();
        String key = criteria.getKey();
        Object value = criteria.getValue();

        switch (operation) {
            case EQUALS -> {
                return builder.equal(root.get(key), value);
            }
            case NOT_EQUALS -> {
                return builder.notEqual(root.get(key), value);
            }
            case LIKE -> {
                return builder.like(root.get(key), "%" + value + "%");
            }
            case GREATER_THAN -> {
                return builder.greaterThan(root.get(key), (Comparable) value);
            }
            case LESS_THAN -> {
                return builder.lessThan(root.get(key), (Comparable) value);
            }
            case GREATER_THAN_OR_EQUAL -> {
                return builder.greaterThanOrEqualTo(root.get(key), (Comparable) value);
            }
            case LESS_THAN_OR_EQUAL -> {
                return builder.lessThanOrEqualTo(root.get(key), (Comparable) value);
            }
            case IN -> {
                if (value instanceof Collection) {
                    return root.get(key).in((Collection<?>) value);
                }
                return root.get(key).in(value);
            }
            case NOT_IN -> {
                if (value instanceof Collection) {
                    return builder.not(root.get(key).in((Collection<?>) value));
                }
                return builder.not(root.get(key).in(value));
            }
            case IS_NULL -> {
                if (Boolean.TRUE.equals(value) || "true".equals(value)) {
                    return builder.isNull(root.get(key));
                } else {
                    return builder.isNotNull(root.get(key));
                }
            }
            case BETWEEN, RANGE -> {
                if (value instanceof Collection<?> collection && collection.size() == 2) {
                    Object[] array = collection.toArray();
                    return builder.between(root.get(key), (Comparable) array[0], (Comparable) array[1]);
                }
                // Fallback or error handling?
                return null;
            }
            default -> {
                return null;
            }
        }
    }
}
