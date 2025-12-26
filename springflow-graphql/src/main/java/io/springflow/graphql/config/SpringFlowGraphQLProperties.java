package io.springflow.graphql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SpringFlow GraphQL support.
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Data
@ConfigurationProperties(prefix = "springflow.graphql")
public class SpringFlowGraphQLProperties {

    /**
     * Enable or disable GraphQL support.
     * Default: false (opt-in feature)
     */
    private boolean enabled = false;

    /**
     * Location where the generated schema.graphqls file will be written.
     * Default: src/main/resources/graphql
     */
    private String schemaLocation = "src/main/resources/graphql";

    /**
     * Enable GraphiQL UI for testing GraphQL queries.
     * Default: true
     */
    private boolean graphiqlEnabled = true;

    /**
     * Enable introspection queries.
     * Default: true (disable in production for security)
     */
    private boolean introspectionEnabled = true;
}
