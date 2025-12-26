package io.springflow.graphql.generator;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.graphql.controller.GenericGraphQLController;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.graphql.data.method.annotation.support.AnnotatedControllerConfigurer;
import org.springframework.stereotype.Component;

/**
 * Generates dynamic GraphQL controller beans for each entity.
 * <p>
 * This generator creates a concrete implementation of {@link GenericGraphQLController}
 * for each entity annotated with @AutoApi, wiring it to the entity's service.
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@Component
public class GraphQLControllerGenerator {

    private final ByteBuddy byteBuddy = new ByteBuddy();

    /**
     * Generates a GraphQL controller bean definition for an entity.
     *
     * @param metadata entity metadata
     * @param registry bean definition registry
     * @return bean name of the generated controller
     */
    public String generateController(EntityMetadata metadata, BeanDefinitionRegistry registry) {
        String entityName = metadata.entityName();
        String controllerBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "GraphQLController";

        log.debug("Generating GraphQL controller for entity: {}", entityName);

        try {
            // Generate dynamic subclass of GenericGraphQLController
            Class<?> controllerClass = createControllerClass(metadata);

            // Create bean definition
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(controllerClass);
            builder.setScope(BeanDefinition.SCOPE_SINGLETON);

            // Constructor args: service, dtoMapper, and metadata
            String serviceBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Service";
            String dtoMapperBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "DtoMapper";
            builder.addConstructorArgReference(serviceBeanName);
            builder.addConstructorArgReference(dtoMapperBeanName);
            builder.addConstructorArgValue(metadata);

            // Register bean
            registry.registerBeanDefinition(controllerBeanName, builder.getBeanDefinition());

            log.info("Registered GraphQL controller: {} for entity {}", controllerBeanName, entityName);
            return controllerBeanName;

        } catch (Exception e) {
            log.error("Failed to generate GraphQL controller for entity: {}", entityName, e);
            throw new RuntimeException("Failed to generate GraphQL controller for " + entityName, e);
        }
    }

    /**
     * Creates a dynamic controller class using ByteBuddy.
     */
    @SuppressWarnings("unchecked")
    private Class<?> createControllerClass(EntityMetadata metadata) {
        String entityName = metadata.entityName();
        String className = entityName + "GraphQLController" + System.currentTimeMillis();

        DynamicType.Builder<?> builder = byteBuddy
                .subclass(GenericGraphQLController.class)
                .name("io.springflow.graphql.generated." + className)
                .annotateType(org.springframework.stereotype.Controller.class.getDeclaredAnnotation(org.springframework.stereotype.Controller.class));

        // Build and load the class
        DynamicType.Unloaded<?> unloadedType = builder.make();
        return unloadedType.load(getClass().getClassLoader()).getLoaded();
    }
}
