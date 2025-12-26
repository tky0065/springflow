package io.springflow.graphql.schema;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Generates GraphQL schema (.graphqls) from entity metadata.
 * <p>
 * For each entity annotated with @AutoApi, this generator creates:
 * - A GraphQL type representing the entity
 * - An input type for create/update operations
 * - Query fields (all, byId)
 * - Mutation fields (create, update, delete)
 * - Pagination types (Page, PageInfo)
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLSchemaGenerator {

    private static final Map<Class<?>, String> TYPE_MAPPING = Map.ofEntries(
            Map.entry(String.class, "String"),
            Map.entry(Integer.class, "Int"),
            Map.entry(int.class, "Int"),
            Map.entry(Long.class, "ID"),
            Map.entry(long.class, "ID"),
            Map.entry(Double.class, "Float"),
            Map.entry(double.class, "Float"),
            Map.entry(Float.class, "Float"),
            Map.entry(float.class, "Float"),
            Map.entry(Boolean.class, "Boolean"),
            Map.entry(boolean.class, "Boolean"),
            Map.entry(LocalDate.class, "String"), // ISO-8601 date
            Map.entry(LocalDateTime.class, "String") // ISO-8601 datetime
    );

    /**
     * Generates a complete GraphQL schema from entity metadata.
     *
     * @param entitiesMetadata list of entity metadata
     * @return GraphQL schema as String
     */
    public String generateSchema(List<EntityMetadata> entitiesMetadata) {
        StringBuilder schema = new StringBuilder();
        schema.append("# SpringFlow Auto-Generated GraphQL Schema\n");
        schema.append("# Generated at: ").append(LocalDateTime.now()).append("\n\n");

        // Generate base types
        schema.append(generateBaseTypes());

        // Generate types and input types for each entity
        for (EntityMetadata metadata : entitiesMetadata) {
            schema.append(generateEntityType(metadata));
            schema.append(generateInputType(metadata));
        }

        // Generate Query type
        schema.append("type Query {\n");
        for (EntityMetadata metadata : entitiesMetadata) {
            schema.append(generateQueries(metadata));
        }
        schema.append("}\n\n");

        // Generate Mutation type
        schema.append("type Mutation {\n");
        for (EntityMetadata metadata : entitiesMetadata) {
            schema.append(generateMutations(metadata));
        }
        schema.append("}\n\n");

        return schema.toString();
    }

    /**
     * Generates base pagination types.
     */
    private String generateBaseTypes() {
        return """
                type PageInfo {
                  pageNumber: Int!
                  pageSize: Int!
                  totalElements: Int!
                  totalPages: Int!
                  hasNext: Boolean!
                  hasPrevious: Boolean!
                }

                """;
    }

    /**
     * Generates GraphQL type for an entity.
     */
    private String generateEntityType(EntityMetadata metadata) {
        StringBuilder type = new StringBuilder();
        String typeName = metadata.entityName();

        type.append("type ").append(typeName).append(" {\n");

        for (FieldMetadata field : metadata.fields()) {
            if (field.hidden()) {
                continue; // Skip @Hidden fields
            }

            String fieldName = field.name();
            String graphQLType = mapJavaTypeToGraphQL(field.type());
            boolean required = !field.nullable() && field.validations().stream()
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("NotNull")
                            || a.annotationType().getSimpleName().equals("NotBlank"));

            type.append("  ").append(fieldName).append(": ").append(graphQLType);
            if (required) {
                type.append("!");
            }
            type.append("\n");
        }

        type.append("}\n\n");
        return type.toString();
    }

    /**
     * Generates GraphQL input type for create/update operations.
     */
    private String generateInputType(EntityMetadata metadata) {
        StringBuilder type = new StringBuilder();
        String typeName = metadata.entityName();

        type.append("input ").append(typeName).append("Input {\n");

        for (FieldMetadata field : metadata.fields()) {
            if (field.hidden() || field.readOnly() || field.isId()) {
                continue; // Skip @Hidden, @ReadOnly, and ID fields in input
            }

            String fieldName = field.name();
            String graphQLType = mapJavaTypeToGraphQL(field.type());
            boolean required = !field.nullable() && field.validations().stream()
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("NotNull")
                            || a.annotationType().getSimpleName().equals("NotBlank"));

            type.append("  ").append(fieldName).append(": ").append(graphQLType);
            if (required) {
                type.append("!");
            }
            type.append("\n");
        }

        type.append("}\n\n");
        return type.toString();
    }

    /**
     * Generates queries for an entity (findAll, findById).
     */
    private String generateQueries(EntityMetadata metadata) {
        String entityName = metadata.entityName();
        String entityNameLower = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
        String pluralName = entityNameLower + "s"; // Simple pluralization

        StringBuilder queries = new StringBuilder();

        // findAll with pagination
        queries.append("  ").append(pluralName)
                .append("(page: Int = 0, size: Int = 20): ")
                .append(entityName).append("Page!\n");

        // findById
        queries.append("  ").append(entityNameLower)
                .append("(id: ID!): ")
                .append(entityName).append("\n");

        // Define Page type for this entity
        queries.append("}\n\n");
        queries.append("type ").append(entityName).append("Page {\n");
        queries.append("  content: [").append(entityName).append("!]!\n");
        queries.append("  pageInfo: PageInfo!\n");

        return queries.toString();
    }

    /**
     * Generates mutations for an entity (create, update, delete).
     */
    private String generateMutations(EntityMetadata metadata) {
        String entityName = metadata.entityName();
        String entityNameLower = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);

        StringBuilder mutations = new StringBuilder();

        // create
        mutations.append("  create").append(entityName)
                .append("(input: ").append(entityName).append("Input!): ")
                .append(entityName).append("!\n");

        // update
        mutations.append("  update").append(entityName)
                .append("(id: ID!, input: ").append(entityName).append("Input!): ")
                .append(entityName).append("!\n");

        // delete
        mutations.append("  delete").append(entityName)
                .append("(id: ID!): Boolean!\n");

        return mutations.toString();
    }

    /**
     * Maps Java types to GraphQL scalar types.
     */
    private String mapJavaTypeToGraphQL(Class<?> javaType) {
        return TYPE_MAPPING.getOrDefault(javaType, "String");
    }

    /**
     * Writes the generated schema to a file.
     *
     * @param schema          the GraphQL schema
     * @param outputDirectory directory to write schema.graphqls
     */
    public void writeSchemaToFile(String schema, String outputDirectory) {
        try {
            Path dirPath = Paths.get(outputDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path schemaPath = dirPath.resolve("schema.graphqls");
            Files.writeString(schemaPath, schema);
            log.info("GraphQL schema generated at: {}", schemaPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write GraphQL schema to file", e);
            throw new RuntimeException("Failed to generate GraphQL schema", e);
        }
    }
}
