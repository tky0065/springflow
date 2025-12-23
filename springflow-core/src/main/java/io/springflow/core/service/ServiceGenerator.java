package io.springflow.core.service;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.repository.RepositoryGenerator;
import io.springflow.core.service.support.SpringFlowServiceFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

/**
 * Generates service beans dynamically for entities annotated with @AutoApi.
 * <p>
 * Creates concrete service implementations by using {@link SpringFlowServiceFactoryBean}
 * and registers them in the Spring application context.
 * </p>
 */
public class ServiceGenerator {

    private final BeanDefinitionRegistry registry;

    public ServiceGenerator(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Generate a service bean for the given entity metadata.
     *
     * @param metadata the entity metadata
     */
    public void generate(EntityMetadata metadata) {
        String serviceBeanName = getServiceBeanName(metadata);
        String repositoryBeanName = RepositoryGenerator.getRepositoryBeanName(metadata);

        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SpringFlowServiceFactoryBean.class)
                .addPropertyValue("entityClass", metadata.entityClass())
                .addPropertyReference("repository", repositoryBeanName)
                .addPropertyValue("metadata", metadata)
                .getBeanDefinition();

        registry.registerBeanDefinition(serviceBeanName, beanDefinition);
    }

    /**
     * Get the service bean name for an entity.
     *
     * @param metadata the entity metadata
     * @return the service bean name (e.g., "productService")
     */
    public static String getServiceBeanName(EntityMetadata metadata) {
        return StringUtils.uncapitalize(metadata.entityName()) + "Service";
    }
}
