package io.springflow.core.repository.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class SpringFlowRepositoryFactoryBean<T, ID> implements FactoryBean<SimpleJpaRepository<T, ID>> {

    private Class<T> entityClass;
    
    @PersistenceContext
    private EntityManager entityManager;

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public SimpleJpaRepository<T, ID> getObject() {
        return new SimpleJpaRepository<>(entityClass, entityManager);
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleJpaRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
