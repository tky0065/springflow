package io.springflow.annotations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 2 annotations: {@link SoftDelete} and {@link Auditable}.
 */
class Phase2AnnotationsTest {

    @SoftDelete
    static class DefaultSoftDeleteEntity {
    }

    @SoftDelete(
            deletedField = "isDeleted",
            deletedAtField = "removedAt"
    )
    static class CustomSoftDeleteEntity {
    }

    @Auditable
    static class DefaultAuditableEntity {
    }

    @Auditable(
            versioned = true,
            createdAtField = "created",
            updatedAtField = "modified",
            createdByField = "author",
            updatedByField = "editor"
    )
    static class CustomAuditableEntity {
    }

    @Test
    void softDeleteShouldBePresent() {
        SoftDelete annotation = DefaultSoftDeleteEntity.class.getAnnotation(SoftDelete.class);
        assertNotNull(annotation, "SoftDelete annotation should be present");
    }

    @Test
    void softDeleteShouldHaveDefaultValues() {
        SoftDelete annotation = DefaultSoftDeleteEntity.class.getAnnotation(SoftDelete.class);

        assertEquals("deleted", annotation.deletedField(),
                "Default deleted field should be 'deleted'");
        assertEquals("deletedAt", annotation.deletedAtField(),
                "Default deletedAt field should be 'deletedAt'");
    }

    @Test
    void softDeleteShouldAllowCustomConfiguration() {
        SoftDelete annotation = CustomSoftDeleteEntity.class.getAnnotation(SoftDelete.class);

        assertEquals("isDeleted", annotation.deletedField());
        assertEquals("removedAt", annotation.deletedAtField());
    }

    @Test
    void auditableShouldBePresent() {
        Auditable annotation = DefaultAuditableEntity.class.getAnnotation(Auditable.class);
        assertNotNull(annotation, "Auditable annotation should be present");
    }

    @Test
    void auditableShouldHaveDefaultValues() {
        Auditable annotation = DefaultAuditableEntity.class.getAnnotation(Auditable.class);

        assertFalse(annotation.versioned(), "Versioning should be disabled by default");
        assertEquals("createdAt", annotation.createdAtField());
        assertEquals("updatedAt", annotation.updatedAtField());
        assertEquals("createdBy", annotation.createdByField());
        assertEquals("updatedBy", annotation.updatedByField());
    }

    @Test
    void auditableShouldAllowCustomConfiguration() {
        Auditable annotation = CustomAuditableEntity.class.getAnnotation(Auditable.class);

        assertTrue(annotation.versioned());
        assertEquals("created", annotation.createdAtField());
        assertEquals("modified", annotation.updatedAtField());
        assertEquals("author", annotation.createdByField());
        assertEquals("editor", annotation.updatedByField());
    }

    @Test
    void shouldBeApplicableToTypes() {
        assertNotNull(DefaultSoftDeleteEntity.class.getAnnotation(SoftDelete.class),
                "SoftDelete should be applicable to classes");
        assertNotNull(DefaultAuditableEntity.class.getAnnotation(Auditable.class),
                "Auditable should be applicable to classes");
    }

    @Test
    void shouldBeRuntimeRetention() {
        assertNotNull(DefaultSoftDeleteEntity.class.getAnnotation(SoftDelete.class),
                "SoftDelete should be available at runtime");
        assertNotNull(DefaultAuditableEntity.class.getAnnotation(Auditable.class),
                "Auditable should be available at runtime");
    }
}
