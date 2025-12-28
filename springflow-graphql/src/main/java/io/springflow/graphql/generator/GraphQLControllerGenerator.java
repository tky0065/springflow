package io.springflow.graphql.generator;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.graphql.controller.GenericGraphQLController;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.attribute.MethodAttributeAppender;
import net.bytebuddy.matcher.ElementMatchers;
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

            // Constructor args: service, dtoMapperFactory, filterResolver, filterConverter, and metadata
            String serviceBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Service";
            builder.addConstructorArgReference(serviceBeanName);
            builder.addConstructorArgReference("dtoMapperFactory");  // Use factory instead of individual mapper
            builder.addConstructorArgReference("filterResolver");
            builder.addConstructorArgReference("graphQLFilterConverter");
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
     * Adds GraphQL annotations (@QueryMapping, @MutationMapping) to methods.
     */
    @SuppressWarnings("unchecked")
    private Class<?> createControllerClass(EntityMetadata metadata) {
        String entityName = metadata.entityName();
        String className = entityName + "GraphQLController" + System.currentTimeMillis();
        String pluralName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "s";
        String singularName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);

        log.debug("Creating GraphQL controller class: {} for entity: {}", className, entityName);

        // Create @Controller annotation
        AnnotationDescription controllerAnnotation = AnnotationDescription.Builder
                .ofType(org.springframework.stereotype.Controller.class)
                .build();

        // Create GraphQL annotations for each method
        AnnotationDescription findAllQuery = AnnotationDescription.Builder
                .ofType(org.springframework.graphql.data.method.annotation.QueryMapping.class)
                .define("name", pluralName)
                .build();

        AnnotationDescription findByIdQuery = AnnotationDescription.Builder
                .ofType(org.springframework.graphql.data.method.annotation.QueryMapping.class)
                .define("name", singularName)
                .build();

        AnnotationDescription createMutation = AnnotationDescription.Builder
                .ofType(org.springframework.graphql.data.method.annotation.MutationMapping.class)
                .define("name", "create" + entityName)
                .build();

        AnnotationDescription updateMutation = AnnotationDescription.Builder
                .ofType(org.springframework.graphql.data.method.annotation.MutationMapping.class)
                .define("name", "update" + entityName)
                .build();

        AnnotationDescription deleteMutation = AnnotationDescription.Builder
                .ofType(org.springframework.graphql.data.method.annotation.MutationMapping.class)
                .define("name", "delete" + entityName)
                .build();

        // Build the class with annotations on methods
        // IMPORTANT: Use ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING to preserve constructor
        // and MethodAttributeAppender to preserve parameter annotations like @Argument
        DynamicType.Builder<?> builder = byteBuddy
                .subclass(GenericGraphQLController.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name("io.springflow.graphql.generated." + className)
                .annotateType(controllerAnnotation)
                // Add @QueryMapping(name="products") to findAll method
                .method(ElementMatchers.named("findAll"))
                .intercept(net.bytebuddy.implementation.SuperMethodCall.INSTANCE)
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateMethod(findAllQuery)
                // Add @QueryMapping(name="product") to findById method
                .method(ElementMatchers.named("findById"))
                .intercept(net.bytebuddy.implementation.SuperMethodCall.INSTANCE)
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateMethod(findByIdQuery)
                // Add @MutationMapping(name="createProduct") to create method
                .method(ElementMatchers.named("create"))
                .intercept(net.bytebuddy.implementation.SuperMethodCall.INSTANCE)
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateMethod(createMutation)
                // Add @MutationMapping(name="updateProduct") to update method
                .method(ElementMatchers.named("update"))
                .intercept(net.bytebuddy.implementation.SuperMethodCall.INSTANCE)
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateMethod(updateMutation)
                // Add @MutationMapping(name="deleteProduct") to delete method
                .method(ElementMatchers.named("delete"))
                .intercept(net.bytebuddy.implementation.SuperMethodCall.INSTANCE)
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateMethod(deleteMutation);

        // Build and load the class with proper ClassLoading strategy
        DynamicType.Unloaded<?> unloadedType = builder.make();
        return unloadedType
                .load(GenericGraphQLController.class.getClassLoader(),
                      net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }
}
