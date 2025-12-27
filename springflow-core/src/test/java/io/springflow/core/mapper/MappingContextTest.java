package io.springflow.core.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for mapping context cycle detection.
 */
class MappingContextTest {

    @Test
    void isBeingMapped_shouldDetectCycles() {
        // Given
        MappingContext context = new MappingContext(DtoMappingConfig.DEFAULT);
        Object entity = new Object();

        // When
        context.enterEntity(entity);

        // Then
        assertThat(context.isBeingMapped(entity)).isTrue();
    }

    @Test
    void exitEntity_shouldRemoveFromVisited() {
        // Given
        MappingContext context = new MappingContext(DtoMappingConfig.DEFAULT);
        Object entity = new Object();
        context.enterEntity(entity);

        // When
        context.exitEntity(entity);

        // Then
        assertThat(context.isBeingMapped(entity)).isFalse();
    }

    @Test
    void isBeingMapped_withCycleDetectionDisabled_shouldReturnFalse() {
        // Given
        DtoMappingConfig config = DtoMappingConfig.builder()
                .detectCycles(false)
                .build();
        MappingContext context = new MappingContext(config);
        Object entity = new Object();

        // When
        context.enterEntity(entity);

        // Then
        assertThat(context.isBeingMapped(entity)).isFalse();
    }

    @Test
    void fork_shouldCreateNewContextWithSameConfig() {
        // Given
        DtoMappingConfig config = DtoMappingConfig.DEEP;
        MappingContext context = new MappingContext(config);

        // When
        MappingContext forked = context.fork();

        // Then
        assertThat(forked.getConfig()).isEqualTo(config);
        assertThat(forked).isNotSameAs(context);
    }

    @Test
    void multipleEntities_shouldTrackAll() {
        // Given
        MappingContext context = new MappingContext(DtoMappingConfig.DEFAULT);
        Object entity1 = new Object();
        Object entity2 = new Object();

        // When
        context.enterEntity(entity1);
        context.enterEntity(entity2);

        // Then
        assertThat(context.isBeingMapped(entity1)).isTrue();
        assertThat(context.isBeingMapped(entity2)).isTrue();
    }
}
