package io.springflow.core.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageablePropertiesTest {

    @Test
    void defaultProperties_shouldHaveCorrectValues() {
        // Given/When
        PageableProperties properties = new PageableProperties();

        // Then
        assertThat(properties.getDefaultPageSize()).isEqualTo(20);
        assertThat(properties.getMaxPageSize()).isEqualTo(100);
        assertThat(properties.getPageParameter()).isEqualTo("page");
        assertThat(properties.getSizeParameter()).isEqualTo("size");
        assertThat(properties.getSortParameter()).isEqualTo("sort");
        assertThat(properties.isOneIndexedParameters()).isFalse();
    }

    @Test
    void setters_shouldUpdateValues() {
        // Given
        PageableProperties properties = new PageableProperties();

        // When
        properties.setDefaultPageSize(50);
        properties.setMaxPageSize(200);
        properties.setPageParameter("p");
        properties.setSizeParameter("s");
        properties.setSortParameter("order");
        properties.setOneIndexedParameters(true);

        // Then
        assertThat(properties.getDefaultPageSize()).isEqualTo(50);
        assertThat(properties.getMaxPageSize()).isEqualTo(200);
        assertThat(properties.getPageParameter()).isEqualTo("p");
        assertThat(properties.getSizeParameter()).isEqualTo("s");
        assertThat(properties.getSortParameter()).isEqualTo("order");
        assertThat(properties.isOneIndexedParameters()).isTrue();
    }
}
