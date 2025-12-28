package io.springflow.graphql.config;

import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.graphql.filter.GraphQLFilterConverter;
import io.springflow.graphql.generator.GraphQLControllerGenerator;
import io.springflow.graphql.schema.GraphQLSchemaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

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
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.graphql.execution.GraphQlSource")
@ConditionalOnProperty(prefix = "springflow.graphql", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SpringFlowGraphQLProperties.class)
@Import({GraphQLControllerRegistrar.class, GraphQLSchemaInitializer.class})
public class SpringFlowGraphQLAutoConfiguration {

    @Bean
    public EntityScanner entityScanner() {
        return new EntityScanner();
    }

    @Bean
    public MetadataResolver metadataResolver() {
        return new MetadataResolver();
    }

    @Bean
    public GraphQLSchemaGenerator graphQLSchemaGenerator() {
        return new GraphQLSchemaGenerator();
    }

    @Bean
    public GraphQLControllerGenerator graphQLControllerGenerator() {
        return new GraphQLControllerGenerator();
    }

    @Bean
    public GraphQLFilterConverter graphQLFilterConverter() {
        return new GraphQLFilterConverter();
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
}
