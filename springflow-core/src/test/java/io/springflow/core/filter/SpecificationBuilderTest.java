package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import io.springflow.core.metadata.testentities.ValidatedEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationBuilderTest {

    @Test
    void shouldBuildSpecificationFromMultipleCriteria() {
        List<SearchCriteria> params = new ArrayList<>();
        params.add(new SearchCriteria("name", FilterType.LIKE, "John"));
        params.add(new SearchCriteria("age", FilterType.GREATER_THAN, 18));

        SpecificationBuilder<ValidatedEntity> builder = new SpecificationBuilder<>();
        Specification<ValidatedEntity> spec = builder.build(params);

        assertNotNull(spec);
    }

    @Test
    void shouldReturnEmptySpecForEmptyParams() {
        List<SearchCriteria> params = new ArrayList<>();
        SpecificationBuilder<ValidatedEntity> builder = new SpecificationBuilder<>();
        Specification<ValidatedEntity> spec = builder.build(params);

        assertNull(spec);
    }
}
