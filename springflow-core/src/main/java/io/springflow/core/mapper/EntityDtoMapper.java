package io.springflow.core.mapper;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reflection-based DTO mapper implementation.
 * <p>
 * This mapper uses {@link EntityMetadata} to intelligently map between entities and Map-based DTOs,
 * respecting @Hidden, @ReadOnly, and other annotations.
 * </p>
 */
public class EntityDtoMapper<T, ID> implements DtoMapper<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(EntityDtoMapper.class);

    private final Class<T> entityClass;
    private final EntityMetadata metadata;

    public EntityDtoMapper(Class<T> entityClass, EntityMetadata metadata) {
        this.entityClass = entityClass;
        this.metadata = metadata;
    }

    @Override
    public T toEntity(Map<String, Object> inputDto) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();

            for (FieldMetadata fieldMeta : metadata.fields()) {
                // Skip ID (auto-generated), hidden fields, and read-only fields for input
                if (fieldMeta.isId() || fieldMeta.hidden() || fieldMeta.readOnly()) {
                    continue;
                }

                String fieldName = fieldMeta.name();
                if (inputDto.containsKey(fieldName)) {
                    Object value = inputDto.get(fieldName);
                    setFieldValue(entity, fieldMeta.field(), value);
                }
            }

            log.debug("Converted InputDTO to entity: {}", entityClass.getSimpleName());
            return entity;

        } catch (Exception e) {
            log.error("Failed to convert InputDTO to entity: {}", entityClass.getSimpleName(), e);
            throw new IllegalArgumentException("Failed to convert DTO to entity: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> toOutputDto(T entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> outputDto = new LinkedHashMap<>();

        for (FieldMetadata fieldMeta : metadata.fields()) {
            // Skip hidden fields in output
            if (fieldMeta.hidden()) {
                continue;
            }

            String fieldName = fieldMeta.name();
            Object value = getFieldValue(entity, fieldMeta.field());
            outputDto.put(fieldName, value);
        }

        log.debug("Converted entity to OutputDTO: {}", entityClass.getSimpleName());
        return outputDto;
    }

    @Override
    public void updateEntity(T entity, Map<String, Object> inputDto) {
        for (FieldMetadata fieldMeta : metadata.fields()) {
            // Skip ID, hidden fields, and read-only fields for updates
            if (fieldMeta.isId() || fieldMeta.hidden() || fieldMeta.readOnly()) {
                continue;
            }

            String fieldName = fieldMeta.name();
            if (inputDto.containsKey(fieldName)) {
                Object value = inputDto.get(fieldName);
                setFieldValue(entity, fieldMeta.field(), value);
            }
        }

        log.debug("Updated entity from InputDTO: {}", entityClass.getSimpleName());
    }

    @Override
    public List<Map<String, Object>> toOutputDtoList(List<T> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toOutputDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> toOutputDtoPage(Page<T> entityPage) {
        if (entityPage == null) {
            return Page.empty();
        }

        List<Map<String, Object>> dtoList = toOutputDtoList(entityPage.getContent());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * Gets field value using reflection, handling access modifiers.
     */
    private Object getFieldValue(T entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.warn("Failed to get field value: {}.{}", entityClass.getSimpleName(), field.getName());
            return null;
        }
    }

    /**
     * Sets field value using reflection, handling type conversion.
     */
    private void setFieldValue(T entity, Field field, Object value) {
        try {
            field.setAccessible(true);

            // Handle null values
            if (value == null) {
                field.set(entity, null);
                return;
            }

            // Handle type conversion if needed
            Object convertedValue = convertValue(value, field.getType());
            field.set(entity, convertedValue);

        } catch (IllegalAccessException e) {
            log.error("Failed to set field value: {}.{}", entityClass.getSimpleName(), field.getName(), e);
            throw new IllegalArgumentException(
                    "Failed to set field " + field.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Converts value to target type if necessary.
     * Handles common type conversions (String to Number, etc.).
     */
    private Object convertValue(Object value, Class<?> targetType) {
        // If types match, no conversion needed
        if (targetType.isInstance(value)) {
            return value;
        }

        // String to primitive/wrapper conversions
        if (value instanceof String) {
            String strValue = (String) value;

            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(strValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(strValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(strValue);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(strValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(strValue);
            }
        }

        // Number to Number conversions
        if (value instanceof Number) {
            Number numValue = (Number) value;

            if (targetType == Integer.class || targetType == int.class) {
                return numValue.intValue();
            } else if (targetType == Long.class || targetType == long.class) {
                return numValue.longValue();
            } else if (targetType == Double.class || targetType == double.class) {
                return numValue.doubleValue();
            } else if (targetType == Float.class || targetType == float.class) {
                return numValue.floatValue();
            }
        }

        // Default: return value as-is and let reflection handle it
        return value;
    }
}
