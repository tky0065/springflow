package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.service.GenericCrudService;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Field;

/**
 * Factory bean for creating concrete controller instances.
 * <p>
 * This factory creates a concrete implementation of {@link GenericCrudController}
 * by extending it and providing the required service dependency and ID extraction logic.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public class SpringFlowControllerFactoryBean<T, ID> implements FactoryBean<GenericCrudController<T, ID>> {

    private Class<T> entityClass;
    private GenericCrudService<T, ID> service;
    private EntityMetadata metadata;

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setService(GenericCrudService<T, ID> service) {
        this.service = service;
    }

    public void setMetadata(EntityMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public GenericCrudController<T, ID> getObject() {
        return new GenericCrudController<T, ID>(service, entityClass) {
            @Override
            protected ID getEntityId(T entity) {
                return extractIdFromEntity(entity);
            }
        };
    }

    @Override
    public Class<?> getObjectType() {
        return GenericCrudController.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Extract the ID value from an entity using reflection.
     *
     * @param entity the entity
     * @return the ID value
     */
    @SuppressWarnings("unchecked")
    private ID extractIdFromEntity(T entity) {
        if (entity == null || metadata == null) {
            return null;
        }

        // Find the ID field from metadata
        FieldMetadata idField = metadata.fields().stream()
                .filter(FieldMetadata::isId)
                .findFirst()
                .orElse(null);

        if (idField == null) {
            throw new IllegalStateException("No ID field found for entity: " + entityClass.getName());
        }

        try {
            Field field = idField.field();
            field.setAccessible(true);
            return (ID) field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract ID from entity: " + entityClass.getName(), e);
        }
    }
}
