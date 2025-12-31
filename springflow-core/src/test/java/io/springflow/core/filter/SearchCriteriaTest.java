package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {

    @Test
    void shouldStoreCriteriaDetails() {
        SearchCriteria criteria = new SearchCriteria("name", FilterType.LIKE, "John");
        assertEquals("name", criteria.getKey());
        assertEquals(FilterType.LIKE, criteria.getOperation());
        assertEquals("John", criteria.getValue());
    }
}
