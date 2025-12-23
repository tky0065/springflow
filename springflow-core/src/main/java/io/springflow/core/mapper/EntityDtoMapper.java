package io.springflow.core.mapper;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;
    private final DtoMapperFactory mapperFactory;
    private static final int DEFAULT_MAX_DEPTH = 1;

    public EntityDtoMapper(Class<T> entityClass, EntityMetadata metadata, EntityManager entityManager, DtoMapperFactory mapperFactory) {
        this.entityClass = entityClass;
        this.metadata = metadata;
        this.entityManager = entityManager;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public T toEntity(Map<String, Object> inputDto) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            applyDtoToEntity(entity, inputDto);
            log.debug("Converted InputDTO to entity: {}", entityClass.getSimpleName());
            return entity;
        } catch (Exception e) {
            log.error("Failed to convert InputDTO to entity: {}", entityClass.getSimpleName(), e);
            throw new IllegalArgumentException("Failed to convert DTO to entity: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> toOutputDto(T entity) {
        return toOutputDto(entity, 0);
    }

    /**
     * Internal method to convert entity to DTO with depth tracking.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toOutputDto(Object entity, int currentDepth) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> outputDto = new LinkedHashMap<>();
        
        // We need metadata for the specific entity being mapped
        // If it's the same class as this mapper, we use its metadata
        if (entity.getClass() == entityClass) {
            for (FieldMetadata fieldMeta : metadata.fields()) {
                if (fieldMeta.hidden()) continue;
                String fieldName = fieldMeta.name();
                Object value = getFieldValue((T) entity, fieldMeta.field());
                if (fieldMeta.isRelation() && value != null) {
                    value = mapRelationValue(value, fieldMeta, currentDepth);
                }
                outputDto.put(fieldName, value);
            }
        } else {
            // For related entities, we should ideally use their own mappers
            // For now, return ID only to avoid complexity
            return Collections.singletonMap("id", getEntityIdValue(entity));
        }

        return outputDto;
    }

    @Override
    public void updateEntity(T entity, Map<String, Object> inputDto) {
        applyDtoToEntity(entity, inputDto);
        log.debug("Updated entity from InputDTO: {}", entityClass.getSimpleName());
    }

    private void applyDtoToEntity(T entity, Map<String, Object> inputDto) {
        for (FieldMetadata fieldMeta : metadata.fields()) {
            if (fieldMeta.isId() || fieldMeta.hidden() || fieldMeta.readOnly()) {
                continue;
            }

            String fieldName = fieldMeta.name();
            if (inputDto.containsKey(fieldName)) {
                Object value = inputDto.get(fieldName);
                if (fieldMeta.isRelation() && value != null) {
                    value = resolveRelationValue(value, fieldMeta);
                }
                setFieldValue(entity, fieldMeta.field(), value);
            }
        }
    }

    private Object resolveRelationValue(Object value, FieldMetadata fieldMeta) {
        Class<?> targetEntity = fieldMeta.relation().targetEntity();
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(id -> id instanceof Map ? null : entityManager.getReference(targetEntity, id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else if (value instanceof Map) {
            return null; // Nested DTO in input not supported for now
        } else if (value != null) {
            return entityManager.getReference(targetEntity, value);
        }
        return null;
    }

    private Object mapRelationValue(Object value, FieldMetadata fieldMeta, int currentDepth) {
        if (currentDepth >= DEFAULT_MAX_DEPTH) {
            return mapToIdOnly(value);
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> mapSingleRelationValue(item, fieldMeta, currentDepth))
                    .collect(Collectors.toList());
        } else {
            return mapSingleRelationValue(value, fieldMeta, currentDepth);
        }
    }

    private Object mapSingleRelationValue(Object item, FieldMetadata fieldMeta, int currentDepth) {
        if (item == null) return null;
        Class<?> targetClass = fieldMeta.relation().targetEntity();
        
        try {
            if (targetClass.isAnnotationPresent(io.springflow.annotations.AutoApi.class)) {
                DtoMapper mapper = mapperFactory.getMapper(targetClass);
                if (mapper instanceof EntityDtoMapper entityMapper) {
                    return entityMapper.toOutputDto(item, currentDepth + 1);
                }
            }
        } catch (Exception e) {
            log.debug("Fallback to ID mapping for {}", targetClass.getSimpleName());
        }
        
        return getEntityIdValue(item);
    }

    private Object mapToIdOnly(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::getEntityIdValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            return getEntityIdValue(value);
        }
    }

    private Object getEntityIdValue(Object entity) {
        if (entity == null) return null;
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class) || 
                    field.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                    field.setAccessible(true);
                    return field.get(entity);
                }
            }
            Class<?> superclass = entity.getClass().getSuperclass();
            while (superclass != null && superclass != Object.class) {
                for (Field field : superclass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(jakarta.persistence.Id.class) || 
                        field.isAnnotationPresent(jakarta.persistence.EmbeddedId.class)) {
                        field.setAccessible(true);
                        return field.get(entity);
                    }
                }
                superclass = superclass.getSuperclass();
            }
        } catch (Exception e) {
            log.warn("Failed to extract ID from related entity: {}", entity.getClass().getName());
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> toOutputDtoList(List<T> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toOutputDto).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> toOutputDtoPage(Page<T> entityPage) {
        if (entityPage == null) return Page.empty();
        List<Map<String, Object>> dtoList = toOutputDtoList(entityPage.getContent());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    private Object getFieldValue(T entity, Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.warn("Failed to get field value: {}.{}", entityClass.getSimpleName(), field.getName());
            return null;
        }
    }

    private void setFieldValue(T entity, Field field, Object value) {
        try {
            field.setAccessible(true);
            if (value == null) {
                field.set(entity, null);
                return;
            }
            Object convertedValue = convertValue(value, field.getType());
            field.set(entity, convertedValue);
        } catch (IllegalAccessException e) {
            log.error("Failed to set field value: {}.{}", entityClass.getSimpleName(), field.getName(), e);
            throw new IllegalArgumentException("Failed to set field " + field.getName() + ": " + e.getMessage(), e);
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) return value;
        if (value instanceof String strValue) {
            if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(strValue);
            if (targetType == Long.class || targetType == long.class) return Long.valueOf(strValue);
            if (targetType == Double.class || targetType == double.class) return Double.valueOf(strValue);
            if (targetType == Float.class || targetType == float.class) return Float.valueOf(strValue);
            if (targetType == Boolean.class || targetType == boolean.class) return Boolean.valueOf(strValue);
        }
        if (value instanceof Number numValue) {
            if (targetType == Integer.class || targetType == int.class) return numValue.intValue();
            if (targetType == Long.class || targetType == long.class) return numValue.longValue();
            if (targetType == Double.class || targetType == double.class) return numValue.doubleValue();
            if (targetType == Float.class || targetType == float.class) return numValue.floatValue();
        }
        return value;
    }
}
