package io.springflow.core.metadata;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Auditable;
import io.springflow.annotations.Filterable;
import io.springflow.annotations.Hidden;
import io.springflow.annotations.ReadOnly;
import io.springflow.annotations.SoftDelete;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves metadata for JPA entities.
 */
public class MetadataResolver {

    private static final Logger log = LoggerFactory.getLogger(MetadataResolver.class);
    
    // JSR-380 Validation annotations to look for
    private static final Set<Class<? extends Annotation>> VALIDATION_ANNOTATIONS = Set.of(
            NotNull.class, NotBlank.class, NotEmpty.class,
            Size.class, Min.class, Max.class,
            Email.class, Pattern.class,
            AssertTrue.class, AssertFalse.class,
            DecimalMin.class, DecimalMax.class,
            Digits.class, Past.class, PastOrPresent.class,
            Future.class, FutureOrPresent.class
    );

    public EntityMetadata resolve(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(AutoApi.class)) {
            throw new IllegalArgumentException("Class must be annotated with @AutoApi: " + entityClass.getName());
        }

        log.debug("Resolving metadata for entity: {}", entityClass.getName());

        String entityName = entityClass.getSimpleName();
        String tableName = determineTableName(entityClass);
        AutoApi autoApi = entityClass.getAnnotation(AutoApi.class);
        SoftDelete softDelete = entityClass.getAnnotation(SoftDelete.class);
        Auditable auditable = entityClass.getAnnotation(Auditable.class);
        
        List<FieldMetadata> fields = resolveFields(entityClass);
        Class<?> idType = resolveIdType(fields);

        return new EntityMetadata(
                entityClass,
                idType,
                entityName,
                tableName,
                autoApi,
                softDelete,
                auditable,
                fields
        );
    }

    private String determineTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                return table.name();
            }
        }
        return convertCamelToSnake(entityClass.getSimpleName());
    }
    
    private String convertCamelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private List<FieldMetadata> resolveFields(Class<?> entityClass) {
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            boolean process = currentClass == entityClass || currentClass.isAnnotationPresent(MappedSuperclass.class);
            
            if (process) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (shouldIncludeField(field)) {
                        fieldMetadataList.add(buildFieldMetadata(field));
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return fieldMetadataList;
    }

    private boolean shouldIncludeField(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && 
               !Modifier.isTransient(modifiers) &&
               !field.isAnnotationPresent(Transient.class);
    }

    private FieldMetadata buildFieldMetadata(Field field) {
        boolean isId = field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
        boolean isNullable = isNullable(field);
        boolean isReadOnly = field.isAnnotationPresent(ReadOnly.class) || isId; 
        
        List<Annotation> validations = extractValidations(field);
        RelationMetadata relation = extractRelation(field);
        Filterable filterConfig = field.getAnnotation(Filterable.class);
        
        GenerationType generationType = null;
        if (field.isAnnotationPresent(GeneratedValue.class)) {
            generationType = field.getAnnotation(GeneratedValue.class).strategy();
        }

        boolean isJsonIgnored = field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class);

        return new FieldMetadata(
                field,
                field.getName(),
                field.getType(),
                isNullable,
                field.isAnnotationPresent(Hidden.class),
                isReadOnly,
                isId,
                generationType,
                validations,
                filterConfig,
                relation,
                isJsonIgnored
        );
    }

    private boolean isNullable(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.nullable()) {
                return false;
            }
        }
        if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotBlank.class)) {
            return false;
        }
        return !field.getType().isPrimitive();
    }

    private List<Annotation> extractValidations(Field field) {
        return Arrays.stream(field.getAnnotations())
                .filter(ann -> VALIDATION_ANNOTATIONS.contains(ann.annotationType()))
                .collect(Collectors.toList());
    }
    
    private RelationMetadata extractRelation(Field field) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            OneToMany ann = field.getAnnotation(OneToMany.class);
            return new RelationMetadata(
                    RelationMetadata.RelationType.ONE_TO_MANY,
                    ann.fetch(),
                    ann.cascade(),
                    ann.targetEntity() == void.class ? getGenericType(field) : ann.targetEntity(),
                    ann.mappedBy()
            );
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne ann = field.getAnnotation(ManyToOne.class);
            return new RelationMetadata(
                    RelationMetadata.RelationType.MANY_TO_ONE,
                    ann.fetch(),
                    ann.cascade(),
                    ann.targetEntity() == void.class ? field.getType() : ann.targetEntity(),
                    null
            );
        } else if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany ann = field.getAnnotation(ManyToMany.class);
            return new RelationMetadata(
                    RelationMetadata.RelationType.MANY_TO_MANY,
                    ann.fetch(),
                    ann.cascade(),
                    ann.targetEntity() == void.class ? getGenericType(field) : ann.targetEntity(),
                    ann.mappedBy()
            );
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne ann = field.getAnnotation(OneToOne.class);
            return new RelationMetadata(
                    RelationMetadata.RelationType.ONE_TO_ONE,
                    ann.fetch(),
                    ann.cascade(),
                    ann.targetEntity() == void.class ? field.getType() : ann.targetEntity(),
                    ann.mappedBy()
            );
        }
        return null;
    }
    
    private Class<?> getGenericType(Field field) {
        java.lang.reflect.Type genericType = field.getGenericType();
        if (genericType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) genericType;
            if (paramType.getActualTypeArguments().length > 0) {
                 java.lang.reflect.Type typeArg = paramType.getActualTypeArguments()[0];
                 if (typeArg instanceof Class) {
                     return (Class<?>) typeArg;
                 }
            }
        }
        return field.getType();
    }

    private Class<?> resolveIdType(List<FieldMetadata> fields) {
        return fields.stream()
                .filter(FieldMetadata::isId)
                .findFirst()
                .map(FieldMetadata::type)
                .orElse(null);
    }
}
