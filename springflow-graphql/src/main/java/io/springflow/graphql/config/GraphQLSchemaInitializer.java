package io.springflow.graphql.config;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.graphql.generator.GraphQLControllerGenerator;
import io.springflow.graphql.schema.GraphQLSchemaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes GraphQL schema and controllers after application context is ready.
 * <p>
 * This component runs after all beans are created and registers dynamic GraphQL
 * controllers for each entity annotated with @AutoApi.
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLSchemaInitializer {

    private final ApplicationContext applicationContext;
    private final EntityScanner entityScanner;
    private final MetadataResolver metadataResolver;
    private final GraphQLSchemaGenerator schemaGenerator;
    private final GraphQLControllerGenerator controllerGenerator;
    private final SpringFlowGraphQLProperties properties;

    private boolean initialized = false;

    /**
     * Initializes GraphQL schema and controllers when application is ready.
     * This runs after all regular beans are created but before the application
     * starts serving requests.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        if (initialized) {
            return; // Prevent multiple initializations
        }

        log.info("Starting SpringFlow GraphQL schema initialization...");

        try {
            // Get base packages
            List<String> basePackages = determineBasePackages();
            log.debug("Scanning packages for @AutoApi entities: {}", basePackages);

            // Scan entities
            List<Class<?>> entities = entityScanner.scanEntities(basePackages.toArray(new String[0]));
            log.info("Found {} entities with @AutoApi for GraphQL generation", entities.size());

            if (entities.isEmpty()) {
                log.warn("No entities found with @AutoApi. GraphQL schema will not be generated.");
                initialized = true;
                return;
            }

            // Resolve metadata for all entities
            List<EntityMetadata> entitiesMetadata = new ArrayList<>();
            for (Class<?> entityClass : entities) {
                EntityMetadata metadata = metadataResolver.resolve(entityClass);
                entitiesMetadata.add(metadata);
            }

            // Generate GraphQL schema
            String schema = schemaGenerator.generateSchema(entitiesMetadata);

            // Write schema to file
            String schemaLocation = properties.getSchemaLocation();
            schemaGenerator.writeSchemaToFile(schema, schemaLocation);

            log.info("SpringFlow GraphQL schema initialization completed. Generated schema for {} entities",
                    entitiesMetadata.size());

            initialized = true;

        } catch (Exception e) {
            log.error("Failed to initialize SpringFlow GraphQL schema", e);
            throw new RuntimeException("SpringFlow GraphQL schema initialization failed", e);
        }
    }

    /**
     * Determines base packages to scan for entities.
     */
    private List<String> determineBasePackages() {
        List<String> packages = new ArrayList<>();

        try {
            Object autoConfigPackages = applicationContext.getBean("org.springframework.boot.autoconfigure.AutoConfigurationPackages");
            String[] pkgs = (String[]) autoConfigPackages.getClass()
                    .getMethod("get", Object.class)
                    .invoke(null, applicationContext);

            if (pkgs != null && pkgs.length > 0) {
                packages.addAll(List.of(pkgs));
            }
        } catch (Exception e) {
            log.debug("Could not determine auto-configuration packages", e);
        }

        if (packages.isEmpty()) {
            packages.add("com");
            packages.add("io");
            log.warn("Using fallback packages for entity scanning: {}", packages);
        }

        return packages;
    }
}
