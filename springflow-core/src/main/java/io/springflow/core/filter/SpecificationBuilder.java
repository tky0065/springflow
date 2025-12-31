package io.springflow.core.filter;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Builder to combine multiple SearchCriteria into a single JPA Specification.
 *
 * @param <T> Entity type
 * @author SpringFlow
 */
public class SpecificationBuilder<T> {

    /**
     * Builds a combined Specification from a list of criteria.
     *
     * @param params List of criteria
     * @return Combined Specification, or null if list is empty
     */
    public Specification<T> build(List<SearchCriteria> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        Specification<T> result = Specification.where(new GenericSpecification<>(params.get(0)));

        for (int i = 1; i < params.size(); i++) {
            result = result.and(new GenericSpecification<>(params.get(i)));
        }

        return result;
    }
}
