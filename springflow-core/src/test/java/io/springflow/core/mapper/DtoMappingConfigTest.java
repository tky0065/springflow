package io.springflow.core.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DTO mapping configuration.
 */
class DtoMappingConfigTest {

    @Test
    void defaultConfig_shouldHaveReasonableDefaults() {
        // When
        DtoMappingConfig config = DtoMappingConfig.DEFAULT;

        // Then
        assertThat(config.getMaxDepth()).isEqualTo(1);
        assertThat(config.isDetectCycles()).isTrue();
        assertThat(config.isIncludeNullFields()).isFalse();
    }

    @Test
    void deepConfig_shouldAllowDeeperNesting() {
        // When
        DtoMappingConfig config = DtoMappingConfig.DEEP;

        // Then
        assertThat(config.getMaxDepth()).isEqualTo(3);
        assertThat(config.isDetectCycles()).isTrue();
    }

    @Test
    void shallowConfig_shouldOnlyIncludeIds() {
        // When
        DtoMappingConfig config = DtoMappingConfig.SHALLOW;

        // Then
        assertThat(config.getMaxDepth()).isEqualTo(0);
    }

    @Test
    void builder_shouldCreateCustomConfig() {
        // When
        DtoMappingConfig config = DtoMappingConfig.builder()
                .maxDepth(5)
                .detectCycles(false)
                .includeNullFields(true)
                .build();

        // Then
        assertThat(config.getMaxDepth()).isEqualTo(5);
        assertThat(config.isDetectCycles()).isFalse();
        assertThat(config.isIncludeNullFields()).isTrue();
    }

    @Test
    void negativeMaxDepth_shouldBeClampedToZero() {
        // When
        DtoMappingConfig config = new DtoMappingConfig(-5, true, false);

        // Then
        assertThat(config.getMaxDepth()).isEqualTo(0);
    }
}
