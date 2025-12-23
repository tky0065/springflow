package io.springflow.core.controller;

import io.springflow.core.controller.support.SpringFlowControllerFactoryBean;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.ServiceGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates controller beans dynamically for entities annotated with @AutoApi.
 * <p>
 * Creates concrete controller implementations by using {@link SpringFlowControllerFactoryBean}
 * and registers them in the Spring application context with proper REST mappings.
 * </p>
 */
public class ControllerGenerator {

    private final BeanDefinitionRegistry registry;

    public ControllerGenerator(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Generate a controller bean for the given entity metadata.
     *
     * @param metadata the entity metadata
     */
    public void generate(EntityMetadata metadata) {
        String controllerBeanName = getControllerBeanName(metadata);
        String serviceBeanName = ServiceGenerator.getServiceBeanName(metadata);
        String basePath = getBasePath(metadata);

        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SpringFlowControllerFactoryBean.class)
                .addPropertyValue("entityClass", metadata.entityClass())
                .addPropertyReference("service", serviceBeanName)
                .addPropertyReference("dtoMapperFactory", "dtoMapperFactory")
                .addPropertyReference("filterResolver", "filterResolver")
                .addPropertyValue("metadata", metadata)
                .getBeanDefinition();

        // Add @RestController and @RequestMapping annotations metadata
        beanDefinition.setAttribute("restController", true);
        beanDefinition.setAttribute("requestMapping", basePath);
        // Add entityMetadata attribute for OpenAPI customizer
        beanDefinition.setAttribute("entityMetadata", metadata);

        registry.registerBeanDefinition(controllerBeanName, beanDefinition);
    }

    /**
     * Get the controller bean name for an entity.
     *
     * @param metadata the entity metadata
     * @return the controller bean name (e.g., "productController")
     */
    public static String getControllerBeanName(EntityMetadata metadata) {
        return StringUtils.uncapitalize(metadata.entityName()) + "Controller";
    }

    /**
     * Get the base path for the controller from @AutoApi annotation.
     * <p>
     * Returns the entity-specific path WITHOUT the global base path,
     * as the global base path will be prepended by RequestMappingRegistrar.
     * </p>
     *
     * @param metadata the entity metadata
     * @return the entity path (e.g., "/products")
     */
    private String getBasePath(EntityMetadata metadata) {
        if (metadata.autoApiConfig() != null && metadata.autoApiConfig().path() != null
                && !metadata.autoApiConfig().path().isEmpty()) {
            return metadata.autoApiConfig().path();
        }

        // Default: /{entityNamePlural} (without global /api prefix)
        String entityNameLower = StringUtils.uncapitalize(metadata.entityName());
        // Simple pluralization (add 's')
        String plural = entityNameLower.endsWith("s") ? entityNameLower : entityNameLower + "s";
        return "/" + plural;
    }
}
