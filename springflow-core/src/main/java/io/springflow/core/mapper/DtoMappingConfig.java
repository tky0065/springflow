package io.springflow.core.mapper;

/**
 * Configuration class for DTO mapping behavior.
 * <p>
 * Controls various aspects of entity-to-DTO mapping including:
 * </p>
 * <ul>
 *   <li>Maximum nesting depth for relations</li>
 *   <li>Cycle detection for bidirectional relations</li>
 *   <li>Field selection patterns</li>
 * </ul>
 *
 * @since 0.4.0
 */
public class DtoMappingConfig {

    private final int maxDepth;
    private final boolean detectCycles;
    private final boolean includeNullFields;

    /**
     * Default configuration with sensible defaults.
     */
    public static final DtoMappingConfig DEFAULT = new DtoMappingConfig(1, true, false);

    /**
     * Configuration for deep nested relations (up to 3 levels).
     */
    public static final DtoMappingConfig DEEP = new DtoMappingConfig(3, true, false);

    /**
     * Configuration for shallow mapping (ID only for relations).
     */
    public static final DtoMappingConfig SHALLOW = new DtoMappingConfig(0, true, false);

    /**
     * Create a custom configuration.
     *
     * @param maxDepth         maximum nesting depth (0 = only IDs, 1 = one level, etc.)
     * @param detectCycles     whether to detect and prevent circular references
     * @param includeNullFields whether to include null fields in output
     */
    public DtoMappingConfig(int maxDepth, boolean detectCycles, boolean includeNullFields) {
        this.maxDepth = Math.max(0, maxDepth);
        this.detectCycles = detectCycles;
        this.includeNullFields = includeNullFields;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public boolean isDetectCycles() {
        return detectCycles;
    }

    public boolean isIncludeNullFields() {
        return includeNullFields;
    }

    /**
     * Create a builder for custom configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxDepth = 1;
        private boolean detectCycles = true;
        private boolean includeNullFields = false;

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder detectCycles(boolean detectCycles) {
            this.detectCycles = detectCycles;
            return this;
        }

        public Builder includeNullFields(boolean includeNullFields) {
            this.includeNullFields = includeNullFields;
            return this;
        }

        public DtoMappingConfig build() {
            return new DtoMappingConfig(maxDepth, detectCycles, includeNullFields);
        }
    }
}
