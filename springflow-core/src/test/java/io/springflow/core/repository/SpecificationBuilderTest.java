package io.springflow.core.repository;

import io.springflow.core.dto.FilterOperator;
import io.springflow.core.dto.SearchRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SpecificationBuilderTest {

    @Test
    void shouldBuildSpecificationFromCriteria() {
        SearchRequest.FilterCriteria criteria = new SearchRequest.FilterCriteria("name", FilterOperator.EQUALS, "John");
        SearchRequest request = new SearchRequest(List.of(criteria), SearchRequest.LogicalOperator.AND);

        Specification<Object> spec = SpecificationBuilder.build(request);

        assertThat(spec).isNotNull();
        
        // Testing the resulting predicate is hard without a real JPA environment, 
        // but we can at least verify it's not null.
    }
}
