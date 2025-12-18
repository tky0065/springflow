package io.springflow.annotations;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for field-level annotations: {@link Hidden} and {@link ReadOnly}.
 */
class FieldAnnotationsTest {

    static class TestEntity {
        @Hidden
        private String hiddenField;

        @ReadOnly
        private String readOnlyField;

        @Hidden
        @ReadOnly
        private String combinedField;

        private String normalField;
    }

    @Test
    void hiddenAnnotationShouldBePresent() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("hiddenField");
        Hidden annotation = field.getAnnotation(Hidden.class);
        assertNotNull(annotation, "Hidden annotation should be present");
    }

    @Test
    void readOnlyAnnotationShouldBePresent() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("readOnlyField");
        ReadOnly annotation = field.getAnnotation(ReadOnly.class);
        assertNotNull(annotation, "ReadOnly annotation should be present");
    }

    @Test
    void shouldSupportMultipleAnnotations() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("combinedField");

        Hidden hidden = field.getAnnotation(Hidden.class);
        ReadOnly readOnly = field.getAnnotation(ReadOnly.class);

        assertNotNull(hidden, "Should have Hidden annotation");
        assertNotNull(readOnly, "Should have ReadOnly annotation");
    }

    @Test
    void normalFieldShouldHaveNoAnnotations() throws NoSuchFieldException {
        Field field = TestEntity.class.getDeclaredField("normalField");

        Hidden hidden = field.getAnnotation(Hidden.class);
        ReadOnly readOnly = field.getAnnotation(ReadOnly.class);

        assertNull(hidden, "Should not have Hidden annotation");
        assertNull(readOnly, "Should not have ReadOnly annotation");
    }

    @Test
    void shouldBeRuntimeRetention() throws NoSuchFieldException {
        Field hiddenField = TestEntity.class.getDeclaredField("hiddenField");
        Field readOnlyField = TestEntity.class.getDeclaredField("readOnlyField");

        assertNotNull(hiddenField.getAnnotation(Hidden.class),
                "Hidden should be available at runtime");
        assertNotNull(readOnlyField.getAnnotation(ReadOnly.class),
                "ReadOnly should be available at runtime");
    }
}
