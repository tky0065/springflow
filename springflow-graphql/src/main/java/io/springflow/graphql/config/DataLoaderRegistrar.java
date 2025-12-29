package io.springflow.graphql.config;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.graphql.dataloader.EntityBatchLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.graphql.execution.BatchLoaderRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers DataLoaders for all entities to enable batch loading.
 * <p>
 * This component solves the N+1 query problem by registering DataLoaders
 * that batch entity fetch requests into single queries.
 * </p>
 * <p>
 * This bean is conditional on the presence of {@link BatchLoaderRegistry},
 * which is provided by Spring Boot GraphQL auto-configuration. If Spring GraphQL
 * is not activated (e.g., missing {@code spring.graphql.graphiql.enabled=true} in
 * configuration), this bean will not be created, allowing REST API functionality
 * to work normally without GraphQL support.
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(BatchLoaderRegistry.class)
public class DataLoaderRegistrar {

    private final ApplicationContext applicationContext;

    /**
     * Registers DataLoaders after application is fully initialized.
     * <p>
     * This ensures all repositories and services are available before
     * attempting to create DataLoaders.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerDataLoaders() {
        log.info("Registering GraphQL DataLoaders...");

        try {
            // Get required beans
            EntityScanner entityScanner = applicationContext.getBean(EntityScanner.class);
            MetadataResolver metadataResolver = applicationContext.getBean(MetadataResolver.class);
            BatchLoaderRegistry registry = applicationContext.getBean(BatchLoaderRegistry.class);

            // Get base packages
            List<String> basePackages = determineBasePackages();

            // Scan entities
            List<Class<?>> entities = entityScanner.scanEntities(basePackages.toArray(new String[0]));
            log.debug("Found {} entities for DataLoader registration", entities.size());

            // Register DataLoader for each entity
            int registeredCount = 0;
            for (Class<?> entityClass : entities) {
                EntityMetadata metadata = metadataResolver.resolve(entityClass);
                String entityName = metadata.entityName();
                String repositoryBeanName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Repository";

                try {
                    JpaRepository<?, ?> repository = (JpaRepository<?, ?>) applicationContext.getBean(repositoryBeanName);
                    String loaderName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Loader";

                    // Create and register DataLoader
                    EntityBatchLoader<?, ?> batchLoader = new EntityBatchLoader<>(repository, metadata, entityName);
                    batchLoader.register(registry, loaderName);

                    registeredCount++;
                    log.debug("Registered DataLoader: {} for entity {}", loaderName, entityName);
                } catch (Exception e) {
                    log.warn("Could not register DataLoader for entity {}: {}", entityName, e.getMessage());
                }
            }

            log.info("GraphQL DataLoader registration completed. Registered {} DataLoaders", registeredCount);

        } catch (Exception e) {
            log.error("Failed to register GraphQL DataLoaders", e);
        }
    }

    /**
     * Determines base packages to scan for entities.
     */
    private List<String> determineBasePackages() {
        List<String> packages = new ArrayList<>();

        try {
            // Use ApplicationContext as BeanFactory to get auto-configuration packages
            packages.addAll(org.springframework.boot.autoconfigure.AutoConfigurationPackages.get(applicationContext));
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
