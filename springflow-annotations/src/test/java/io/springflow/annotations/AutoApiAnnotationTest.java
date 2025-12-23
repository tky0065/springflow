package io.springflow.annotations;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AutoApi} annotation.
 */
class AutoApiAnnotationTest {

    @AutoApi
    static class MinimalEntity {
    }

    @AutoApi(
            path = "custom/path",
            expose = Expose.READ_ONLY,
            security = @Security(level = SecurityLevel.AUTHENTICATED),
            pagination = false,
            sorting = false,
            description = "Test API",
            tags = {"test", "demo"}
    )
    static class FullyConfiguredEntity {
    }

    @Test
    void shouldBeRuntimeRetention() {
        AutoApi annotation = MinimalEntity.class.getAnnotation(AutoApi.class);
        assertNotNull(annotation, "Annotation should be present at runtime");
    }

    @Test
    void shouldHaveDefaultValues() {
        AutoApi annotation = MinimalEntity.class.getAnnotation(AutoApi.class);

        assertEquals("", annotation.path(), "Default path should be empty");
        assertEquals(Expose.ALL, annotation.expose(), "Default expose should be ALL");
        assertTrue(annotation.pagination(), "Pagination should be enabled by default");
        assertTrue(annotation.sorting(), "Sorting should be enabled by default");
        assertEquals("", annotation.description(), "Default description should be empty");
        assertEquals(0, annotation.tags().length, "Default tags should be empty");

        // Security defaults
        assertEquals(SecurityLevel.PUBLIC, annotation.security().level(),
                "Default security level should be PUBLIC");
        assertEquals(0, annotation.security().roles().length,
                "Default roles should be empty");
        assertEquals(0, annotation.security().authorities().length,
                "Default authorities should be empty");
    }

    @Test
    void shouldAllowCustomConfiguration() {
        AutoApi annotation = FullyConfiguredEntity.class.getAnnotation(AutoApi.class);

        assertEquals("custom/path", annotation.path());
        assertEquals(Expose.READ_ONLY, annotation.expose());
        assertFalse(annotation.pagination());
        assertFalse(annotation.sorting());
        assertEquals("Test API", annotation.description());
        assertArrayEquals(new String[]{"test", "demo"}, annotation.tags());

        // Security configuration
        assertEquals(SecurityLevel.AUTHENTICATED, annotation.security().level());
    }

    @Test
    void shouldSupportAllExposeValues() {
        assertEquals(4, Expose.values().length, "Should have 4 Expose values");
        assertNotNull(Expose.valueOf("ALL"));
        assertNotNull(Expose.valueOf("READ_ONLY"));
        assertNotNull(Expose.valueOf("CREATE_UPDATE"));
        assertNotNull(Expose.valueOf("CUSTOM"));
    }

    @Test
    void shouldSupportAllSecurityLevels() {
        SecurityLevel[] levels = SecurityLevel.values();
        assertEquals(4, levels.length, "Should have 4 SecurityLevel values");
        // Verify all levels are present
        boolean hasPublic = false, hasAuth = false, hasRole = false, hasUndefined = false;
        for (SecurityLevel level : levels) {
            if (level == SecurityLevel.PUBLIC) hasPublic = true;
            if (level == SecurityLevel.AUTHENTICATED) hasAuth = true;
            if (level == SecurityLevel.ROLE_BASED) hasRole = true;
            if (level == SecurityLevel.UNDEFINED) hasUndefined = true;
        }
        assertTrue(hasPublic && hasAuth && hasRole && hasUndefined, "All SecurityLevel values should be present");
    }

    @Test
    void shouldBeApplicableToTypes() {
        AutoApi annotation = MinimalEntity.class.getAnnotation(AutoApi.class);
        assertNotNull(annotation, "Should be applicable to classes");
    }
}
