package io.springflow.annotations;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Filterable} annotation.
 */
class FilterableAnnotationTest {

    static class TestEntity {
        @Filterable
        private String defaultField;

        @Filterable(types = FilterType.LIKE)
        private String nameField;

        @Filterable(types = {FilterType.GREATER_THAN, FilterType.LESS_THAN})
        private Integer numericField;

        @Filterable(
                types = FilterType.RANGE,
                paramName = "customParam",
                description = "Test filter",
                caseSensitive = false
        )
        private String fullyConfiguredField;
    }

    @Test
    void shouldBeRuntimeRetention() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        Filterable annotation = field.getAnnotation(Filterable.class);
        assertNotNull(annotation, "Annotation should be present at runtime");
    }

    @Test
    void shouldHaveDefaultValues() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        Filterable annotation = field.getAnnotation(Filterable.class);

        assertArrayEquals(new FilterType[]{FilterType.EQUALS}, annotation.types(),
                "Default filter type should be EQUALS");
        assertEquals("", annotation.paramName(), "Default paramName should be empty");
        assertEquals("", annotation.description(), "Default description should be empty");
        assertTrue(annotation.caseSensitive(), "Should be case-sensitive by default");
    }

    @Test
    void shouldSupportSingleFilterType() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("nameField");
        Filterable annotation = field.getAnnotation(Filterable.class);

        assertEquals(1, annotation.types().length);
        assertEquals(FilterType.LIKE, annotation.types()[0]);
    }

    @Test
    void shouldSupportMultipleFilterTypes() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("numericField");
        Filterable annotation = field.getAnnotation(Filterable.class);

        assertEquals(2, annotation.types().length);
        assertEquals(FilterType.GREATER_THAN, annotation.types()[0]);
        assertEquals(FilterType.LESS_THAN, annotation.types()[1]);
    }

    @Test
    void shouldAllowFullConfiguration() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("fullyConfiguredField");
        Filterable annotation = field.getAnnotation(Filterable.class);

        assertEquals(FilterType.RANGE, annotation.types()[0]);
        assertEquals("customParam", annotation.paramName());
        assertEquals("Test filter", annotation.description());
        assertFalse(annotation.caseSensitive());
    }

    @Test
    void shouldSupportAllFilterTypes() {
        assertEquals(11, FilterType.values().length, "Should have 11 FilterType values");

        assertNotNull(FilterType.valueOf("EQUALS"));
        assertNotNull(FilterType.valueOf("LIKE"));
        assertNotNull(FilterType.valueOf("GREATER_THAN"));
        assertNotNull(FilterType.valueOf("LESS_THAN"));
        assertNotNull(FilterType.valueOf("GREATER_THAN_OR_EQUAL"));
        assertNotNull(FilterType.valueOf("LESS_THAN_OR_EQUAL"));
        assertNotNull(FilterType.valueOf("RANGE"));
        assertNotNull(FilterType.valueOf("IN"));
        assertNotNull(FilterType.valueOf("NOT_IN"));
        assertNotNull(FilterType.valueOf("IS_NULL"));
        assertNotNull(FilterType.valueOf("BETWEEN"));
    }

    @Test
    void shouldBeApplicableToFields() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("defaultField");
        Filterable annotation = field.getAnnotation(Filterable.class);
        assertNotNull(annotation, "Should be applicable to fields");
    }
}
