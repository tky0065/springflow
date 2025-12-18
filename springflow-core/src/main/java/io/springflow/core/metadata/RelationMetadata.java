package io.springflow.core.metadata;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

/**
 * Metadata for a JPA relation.
 */
public record RelationMetadata(
    RelationType type,
    FetchType fetchType,
    CascadeType[] cascadeTypes,
    Class<?> targetEntity,
    String mappedBy
) {
    public enum RelationType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }
}
