package io.springflow.core.metadata;

import io.springflow.annotations.Filterable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for a field in an entity.
 */
public record FieldMetadata(
    Field field,
    String name,
    Class<?> type,
    boolean nullable,
    boolean hidden,
    boolean readOnly,
    boolean isId,
    boolean isVersion,
    jakarta.persistence.GenerationType generationType,
    List<Annotation> validations,
    Filterable filterConfig,
    RelationMetadata relation,
    boolean jsonIgnored,
    boolean summary
) {
    public FieldMetadata(Field field, String name, Class<?> type, boolean nullable, boolean hidden, 
                         boolean readOnly, boolean isId, boolean isVersion, 
                         jakarta.persistence.GenerationType generationType, List<Annotation> validations, 
                         Filterable filterConfig, RelationMetadata relation, boolean jsonIgnored) {
        this(field, name, type, nullable, hidden, readOnly, isId, isVersion, generationType, validations, filterConfig, relation, jsonIgnored, false);
    }

    public FieldMetadata {
        if (validations == null) {
            validations = Collections.emptyList();
        }
    }

    public boolean isRelation() {
        return relation != null;
    }
}
