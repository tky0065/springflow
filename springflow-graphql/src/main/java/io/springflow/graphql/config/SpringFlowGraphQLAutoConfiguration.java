package io.springflow.graphql.config;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.graphql.generator.GraphQLControllerGenerator;
import io.springflow.graphql.schema.GraphQLSchemaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for SpringFlow GraphQL support.
 * <p>
 * This configuration automatically generates GraphQL schema and controllers
 * for all entities annotated with @AutoApi when GraphQL is enabled.
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@AutoConfiguration(after = JpaRepositoriesAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.graphql.execution.GraphQlSource")
@ConditionalOnProperty(prefix = "springflow.graphql", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(SpringFlowGraphQLProperties.class)
@RequiredArgsConstructor
public class SpringFlowGraphQLAutoConfiguration implements BeanFactoryPostProcessor {

    @Bean
    public GraphQLSchemaGenerator graphQLSchemaGenerator() {
        return new GraphQLSchemaGenerator();
    }

    @Bean
    public GraphQLControllerGenerator graphQLControllerGenerator() {
        return new GraphQLControllerGenerator();
    }

    /**
     * Configures DataLoaders for batch loading entities.
     * This solves the N+1 query problem when loading related entities in GraphQL.
     * <p>
     * Note: This requires entities and repositories to be already initialized.
     * DataLoaders will be registered on-demand by GraphQL controllers.
     * </p>
     */
    @Bean
    public DataLoaderRegistrar dataLoaderRegistrar(ApplicationContext applicationContext) {
        return new DataLoaderRegistrar(applicationContext);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            log.warn("BeanFactory is not a BeanDefinitionRegistry. GraphQL controllers will not be generated.");
            return;
        }

        log.info("Starting SpringFlow GraphQL auto-configuration...");

        try {
            // Get required beans
            EntityScanner entityScanner = beanFactory.getBean(EntityScanner.class);
            MetadataResolver metadataResolver = beanFactory.getBean(MetadataResolver.class);
            GraphQLSchemaGenerator schemaGenerator = beanFactory.getBean(GraphQLSchemaGenerator.class);
            GraphQLControllerGenerator controllerGenerator = beanFactory.getBean(GraphQLControllerGenerator.class);
            SpringFlowGraphQLProperties properties = beanFactory.getBean(SpringFlowGraphQLProperties.class);

            // Scan entities
            List<String> basePackages = determineBasePackages(beanFactory);
            log.debug("Scanning packages for @AutoApi entities: {}", basePackages);

            List<Class<?>> entities = entityScanner.scanEntities(basePackages.toArray(new String[0]));
            log.info("Found {} entities with @AutoApi for GraphQL generation", entities.size());

            if (entities.isEmpty()) {
                log.warn("No entities found with @AutoApi. GraphQL schema will not be generated.");
                return;
            }

            // Resolve metadata for all entities
            List<EntityMetadata> entitiesMetadata = new ArrayList<>();
            for (Class<?> entityClass : entities) {
                EntityMetadata metadata = metadataResolver.resolve(entityClass);
                entitiesMetadata.add(metadata);

                // Generate GraphQL controller for this entity
                controllerGenerator.generateController(metadata, registry);
            }

            // Generate GraphQL schema
            String schema = schemaGenerator.generateSchema(entitiesMetadata);

            // Write schema to file
            String schemaLocation = properties.getSchemaLocation();
            schemaGenerator.writeSchemaToFile(schema, schemaLocation);

            log.info("SpringFlow GraphQL auto-configuration completed. Generated schema and {} controllers",
                    entitiesMetadata.size());

        } catch (Exception e) {
            log.error("Failed to configure SpringFlow GraphQL", e);
            throw new RuntimeException("SpringFlow GraphQL auto-configuration failed", e);
        }
    }

    /**
     * Determines base packages to scan for entities.
     */
    private List<String> determineBasePackages(ConfigurableListableBeanFactory beanFactory) {
        List<String> packages = new ArrayList<>();

        // Try to get from AutoConfigurationPackages (Spring Boot)
        try {
            String[] autoConfigPackages = (String[]) beanFactory.getBean("org.springframework.boot.autoconfigure.AutoConfigurationPackages")
                    .getClass().getMethod("get", Object.class)
                    .invoke(null, beanFactory);

            if (autoConfigPackages != null && autoConfigPackages.length > 0) {
                packages.addAll(List.of(autoConfigPackages));
            }
        } catch (Exception e) {
            log.debug("Could not determine auto-configuration packages", e);
        }

        // Fallback to scanning common packages if none found
        if (packages.isEmpty()) {
            packages.add("com");
            packages.add("io");
            log.warn("Using fallback packages for entity scanning: {}", packages);
        }

        return packages;
    }
}
