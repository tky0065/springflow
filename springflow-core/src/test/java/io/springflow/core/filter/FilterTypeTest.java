package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilterTypeTest {

    @Test
    void shouldHaveRequiredEnumValues() {
        assertNotNull(FilterType.valueOf("EQUALS"));
        assertNotNull(FilterType.valueOf("LIKE"));
        assertNotNull(FilterType.valueOf("IN"));
        assertNotNull(FilterType.valueOf("GREATER_THAN"));
        assertNotNull(FilterType.valueOf("LESS_THAN"));
        assertNotNull(FilterType.valueOf("GREATER_THAN_OR_EQUAL"));
        assertNotNull(FilterType.valueOf("LESS_THAN_OR_EQUAL"));
        assertNotNull(FilterType.valueOf("NOT_EQUALS"));
        assertNotNull(FilterType.valueOf("BETWEEN"));
    }
}
