package io.springflow.core.service.support;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.GenericCrudService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Factory bean for creating concrete service instances.
 * <p>
 * This factory creates a concrete implementation of {@link GenericCrudService}
 * by extending it and providing the required repository dependency.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public class SpringFlowServiceFactoryBean<T, ID> implements FactoryBean<GenericCrudService<T, ID>> {

    private Class<T> entityClass;
    private JpaRepository<T, ID> repository;
    private EntityMetadata metadata;

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setRepository(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    public void setMetadata(EntityMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public GenericCrudService<T, ID> getObject() {
        return new GenericCrudService<T, ID>(repository, entityClass, metadata) {
            // Anonymous concrete implementation
            // Inherits all methods from GenericCrudService
        };
    }

    @Override
    public Class<?> getObjectType() {
        return GenericCrudService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
