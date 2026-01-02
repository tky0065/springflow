package io.springflow.core.mapper;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    private final int maxDepth;
    private static final int DEFAULT_MAX_DEPTH = 1;

    public EntityDtoMapper(Class<T> entityClass, EntityMetadata metadata, EntityManager entityManager, DtoMapperFactory mapperFactory) {
        this(entityClass, metadata, entityManager, mapperFactory, DEFAULT_MAX_DEPTH);
    }

    public EntityDtoMapper(Class<T> entityClass, EntityMetadata metadata, EntityManager entityManager, DtoMapperFactory mapperFactory, int maxDepth) {
        this.entityClass = entityClass;
        this.metadata = metadata;
        this.entityManager = entityManager;
        this.mapperFactory = mapperFactory;
        this.maxDepth = maxDepth;
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
        return toOutputDto(entity, null);
    }

    @Override
    public Map<String, Object> toOutputDto(T entity, List<String> fields) {
        log.debug("Mapping {} to OutputDTO", entityClass.getSimpleName());
        Object result = toOutputDtoInternal(entity, fields, new MappingContext());
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        } else if (result != null) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", result);
            return map;
        }
        if (entity != null) {
            log.debug("Fallback to ID mapping for {}", entity.getClass().getSimpleName());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", getEntityIdValue(entity));
            return map;
        }
        return null;
    }

    /**
     * Internal method to convert entity to DTO with depth tracking and field filtering.
     */
    @SuppressWarnings("unchecked")
    public Object toOutputDtoInternal(Object entity, List<String> fields, MappingContext context) {
        if (entity == null) {
            return null;
        }

        // Circular reference detection
        if (context.isBeingMapped(entity)) {
            log.debug("Cycle detected for {}, returning summary/ID", entity.getClass().getSimpleName());
            return mapSingleToSummaryOrId(entity);
        }

        if (context.getCurrentDepth() > 0 && context.getCurrentDepth() >= maxDepth && (fields == null || fields.isEmpty())) {
            log.debug("Max depth reached for {}, returning summary/ID", entity.getClass().getSimpleName());
            return mapSingleToSummaryOrId(entity);
        }

        context.enterEntity(entity);
        context.incrementDepth();

        try {
            EntityMetadata entityMetadata = resolveMetadata(entity.getClass());
            
            if (entityMetadata != null) {
                Map<String, Object> outputDto = new LinkedHashMap<>();
                for (FieldMetadata fieldMeta : entityMetadata.fields()) {
                    if (fieldMeta.hidden() || fieldMeta.jsonIgnored()) continue;
                    String fieldName = fieldMeta.name();

                    if (fields != null && !fields.isEmpty()) {
                        boolean exactMatch = fields.contains(fieldName);
                        boolean subFieldMatch = fields.stream().anyMatch(f -> f.startsWith(fieldName + "."));
                        if (!exactMatch && !subFieldMatch) continue;
                    }

                    Object value = getFieldValue(entity, fieldMeta.field());
                    if (fieldMeta.isRelation() && value != null) {
                        List<String> subFields = null;
                        if (fields != null) {
                            subFields = fields.stream()
                                    .filter(f -> f.startsWith(fieldName + "."))
                                    .map(f -> f.substring(fieldName.length() + 1))
                                    .collect(Collectors.toList());
                        }
                        value = mapRelationValue(value, fieldMeta, subFields, context);
                    }
                    outputDto.put(fieldName, value);
                }
                return outputDto;
            } else {
                log.debug("No metadata for {}, returning summary/ID", entity.getClass().getSimpleName());
                return mapSingleToSummaryOrId(entity);
            }
        } finally {
            context.decrementDepth();
            context.exitEntity(entity);
        }
    }

    private EntityMetadata resolveMetadata(Class<?> clazz) {
        if (clazz == entityClass) return metadata;
        try {
            DtoMapper mapper = mapperFactory.getMapper(clazz);
            if (mapper instanceof EntityDtoMapper entityMapper) {
                return entityMapper.metadata;
            }
        } catch (Exception e) {
            log.trace("Could not resolve specific metadata for {}", clazz.getSimpleName());
        }
        return null;
    }

    @Override
    public void validateUpdatableFields(Map<String, Object> inputDto) {
        for (String fieldName : inputDto.keySet()) {
            metadata.fields().stream()
                    .filter(field -> field.name().equals(fieldName))
                    .findFirst()
                    .ifPresent(field -> {
                        if (field.hidden()) {
                            throw new IllegalArgumentException("Cannot update hidden field: " + fieldName);
                        }
                        if (field.readOnly()) {
                            throw new IllegalArgumentException("Cannot update read-only field: " + fieldName);
                        }
                    });
        }
    }

    @Override
    public void updateEntity(T entity, Map<String, Object> inputDto) {
        applyDtoToEntity(entity, inputDto);
        log.debug("Updated entity from InputDTO: {}", entityClass.getSimpleName());
    }

    private void applyDtoToEntity(T entity, Map<String, Object> inputDto) {
        for (FieldMetadata fieldMeta : metadata.fields()) {
            if (fieldMeta.isId() || fieldMeta.hidden() || fieldMeta.readOnly() || fieldMeta.jsonIgnored()) {
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

    private Object resolveRelationValue(Object value, FieldMetadata fieldMetadata) {
        Class<?> targetEntity = fieldMetadata.relation().targetEntity();
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> resolveSingleRelationValue(item, targetEntity))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            return resolveSingleRelationValue(value, targetEntity);
        }
    }

    private Object resolveSingleRelationValue(Object value, Class<?> targetEntity) {
        if (value == null) return null;

        if (value instanceof Map<?, ?> map) {
            try {
                DtoMapper mapper = mapperFactory.getMapper(targetEntity);
                return mapper.toEntity((Map<String, Object>) map);
            } catch (Exception e) {
                log.warn("Failed to map nested DTO for {}: {}", targetEntity.getSimpleName(), e.getMessage());
                return null;
            }
        } else {
            return entityManager.getReference(targetEntity, value);
        }
    }

    private Object mapRelationValue(Object value, FieldMetadata fieldMetadata, List<String> subFields, MappingContext context) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> mapSingleRelationValue(item, fieldMetadata, subFields, context))
                    .collect(Collectors.toList());
        } else {
            return mapSingleRelationValue(value, fieldMetadata, subFields, context);
        }
    }

    private Object mapSingleRelationValue(Object item, FieldMetadata fieldMeta, List<String> subFields, MappingContext context) {
        if (item == null) return null;

        // Force summary projection if the relationship field itself is marked with @Summary
        // but only if no specific sub-fields are requested
        if (fieldMeta.summary() && (subFields == null || subFields.isEmpty())) {
            log.debug("Relationship field {} marked as summary, returning summary/ID", fieldMeta.name());
            return mapSingleToSummaryOrId(item);
        }

        Class<?> targetClass = fieldMeta.relation().targetEntity();
        log.debug("Mapping relation field {} to type {}", fieldMeta.name(), targetClass.getSimpleName());
        
        try {
            DtoMapper mapper = mapperFactory.getMapper(targetClass);
            if (mapper instanceof EntityDtoMapper entityMapper) {
                log.debug("Using EntityDtoMapper for recursive mapping of {}", targetClass.getSimpleName());
                return entityMapper.toOutputDtoInternal(item, subFields, context);
            } else {
                log.debug("No EntityDtoMapper found for {}, falling back to summary/ID", targetClass.getSimpleName());
            }
        } catch (Exception e) {
            log.debug("Error getting mapper for {}: {}, falling back to summary/ID", targetClass.getSimpleName(), e.getMessage());
        }
        
        return mapSingleToSummaryOrId(item);
    }

    private Object mapToIdOnly(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::mapSingleToSummaryOrId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            return mapSingleToSummaryOrId(value);
        }
    }

    private Object mapSingleToSummaryOrId(Object value) {
        if (value == null) return null;
        
        EntityMetadata entityMetadata = resolveMetadata(value.getClass());
        if (entityMetadata == null) {
            return getEntityIdValue(value);
        }

        List<FieldMetadata> summaryFields = entityMetadata.fields().stream()
                .filter(FieldMetadata::summary)
                .collect(Collectors.toList());

        if (summaryFields.isEmpty()) {
            return getEntityIdValue(value);
        }

        Map<String, Object> summaryDto = new LinkedHashMap<>();
        summaryDto.put("id", getEntityIdValue(value));
        
        for (FieldMetadata fieldMeta : summaryFields) {
            if (fieldMeta.isId()) continue;
            summaryDto.put(fieldMeta.name(), getFieldValue(value, fieldMeta.field()));
        }
        
        return summaryDto;
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
        return toOutputDtoList(entities, null);
    }

    @Override
    public List<Map<String, Object>> toOutputDtoList(List<T> entities, List<String> fields) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(e -> toOutputDto(e, fields)).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> toOutputDtoPage(Page<T> entityPage) {
        return toOutputDtoPage(entityPage, null);
    }

    @Override
    public Page<Map<String, Object>> toOutputDtoPage(Page<T> entityPage, List<String> fields) {
        if (entityPage == null) return Page.empty();
        List<Map<String, Object>> dtoList = toOutputDtoList(entityPage.getContent(), fields);
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    private Object getFieldValue(Object entity, Field field) {
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
            if (targetType == BigDecimal.class) return new BigDecimal(strValue);
            if (targetType == BigInteger.class) return new BigInteger(strValue);
        }

        if (value instanceof Number numValue) {
            if (targetType == Integer.class || targetType == int.class) return numValue.intValue();
            if (targetType == Long.class || targetType == long.class) return numValue.longValue();
            if (targetType == Double.class || targetType == double.class) return numValue.doubleValue();
            if (targetType == Float.class || targetType == float.class) return numValue.floatValue();
            if (targetType == BigDecimal.class) {
                if (numValue instanceof BigDecimal) return numValue;
                if (numValue instanceof BigInteger) return new BigDecimal((BigInteger) numValue);
                if (numValue instanceof Double || numValue instanceof Float) {
                    return new BigDecimal(numValue.toString());
                }
                return BigDecimal.valueOf(numValue.longValue());
            }
            if (targetType == BigInteger.class) {
                if (numValue instanceof BigInteger) return numValue;
                if (numValue instanceof BigDecimal) return ((BigDecimal) numValue).toBigInteger();
                return BigInteger.valueOf(numValue.longValue());
            }
        }

        return value;
    }
}