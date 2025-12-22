package io.springflow.starter.openapi;

import io.springflow.core.metadata.EntityMetadata;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Builds OpenAPI Operation objects for CRUD endpoints.
 * <p>
 * Creates operation definitions for all 5 CRUD methods (findAll, findById, create, update, delete)
 * with proper parameters, request bodies, responses, and tags.
 * </p>
 */
public class OperationBuilder {

    private static final Logger log = LoggerFactory.getLogger(OperationBuilder.class);
    private final SchemaGenerator schemaGenerator;

    public OperationBuilder() {
        this.schemaGenerator = new SchemaGenerator();
    }

    /**
     * Build findAll operation (GET /).
     * Includes pagination parameters.
     *
     * @param metadata the entity metadata
     * @param tags     the operation tags
     * @return configured Operation
     */
    public Operation buildFindAllOperation(EntityMetadata metadata, String[] tags) {
        log.debug("Building findAll operation for {}", metadata.entityName());

        Operation operation = new Operation();
        operation.setSummary("List all " + metadata.entityName());
        operation.setDescription("Retrieve a paginated list of all " + metadata.entityName() + " entities. Supports pagination and sorting.");
        operation.setTags(Arrays.asList(tags));
        operation.setOperationId("findAll" + metadata.entityName());

        // Add pagination parameters
        operation.addParametersItem(createPageParameter());
        operation.addParametersItem(createSizeParameter());
        operation.addParametersItem(createSortParameter());

        // Add 200 OK response
        ApiResponses responses = new ApiResponses();
        ApiResponse response200 = new ApiResponse();
        response200.setDescription("Successfully retrieved list");

        Content content = new Content();
        MediaType mediaType = new MediaType();

        // Create a page schema
        Schema<?> pageSchema = createPageSchema(metadata);
        mediaType.setSchema(pageSchema);
        content.addMediaType("application/json", mediaType);
        response200.setContent(content);

        responses.addApiResponse("200", response200);
        operation.setResponses(responses);

        return operation;
    }

    /**
     * Build findById operation (GET /{id}).
     *
     * @param metadata the entity metadata
     * @param tags     the operation tags
     * @return configured Operation
     */
    public Operation buildFindByIdOperation(EntityMetadata metadata, String[] tags) {
        log.debug("Building findById operation for {}", metadata.entityName());

        Operation operation = new Operation();
        operation.setSummary("Get " + metadata.entityName() + " by ID");
        operation.setDescription("Retrieve a single " + metadata.entityName() + " entity by its unique identifier.");
        operation.setTags(Arrays.asList(tags));
        operation.setOperationId("findById" + metadata.entityName());

        // Add ID path parameter
        operation.addParametersItem(createIdPathParameter(metadata));

        // Add responses
        ApiResponses responses = new ApiResponses();

        // 200 OK
        ApiResponse response200 = new ApiResponse();
        response200.setDescription("Successfully retrieved entity");
        Content content200 = new Content();
        MediaType mediaType200 = new MediaType();
        mediaType200.setSchema(schemaGenerator.generateOutputSchema(metadata));
        content200.addMediaType("application/json", mediaType200);
        response200.setContent(content200);
        responses.addApiResponse("200", response200);

        // 404 Not Found
        ApiResponse response404 = new ApiResponse();
        response404.setDescription("Entity not found");
        responses.addApiResponse("404", response404);

        operation.setResponses(responses);

        return operation;
    }

    /**
     * Build create operation (POST /).
     *
     * @param metadata the entity metadata
     * @param tags     the operation tags
     * @return configured Operation
     */
    public Operation buildCreateOperation(EntityMetadata metadata, String[] tags) {
        log.debug("Building create operation for {}", metadata.entityName());

        Operation operation = new Operation();
        operation.setSummary("Create a new " + metadata.entityName());
        operation.setDescription("Create a new " + metadata.entityName() + " entity with the provided data. The request body is validated.");
        operation.setTags(Arrays.asList(tags));
        operation.setOperationId("create" + metadata.entityName());

        // Add request body
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription(metadata.entityName() + " data to create");
        requestBody.setRequired(true);
        Content requestContent = new Content();
        MediaType requestMediaType = new MediaType();
        requestMediaType.setSchema(schemaGenerator.generateInputSchema(metadata));
        requestContent.addMediaType("application/json", requestMediaType);
        requestBody.setContent(requestContent);
        operation.setRequestBody(requestBody);

        // Add responses
        ApiResponses responses = new ApiResponses();

        // 201 Created
        ApiResponse response201 = new ApiResponse();
        response201.setDescription("Entity successfully created");
        Content content201 = new Content();
        MediaType mediaType201 = new MediaType();
        mediaType201.setSchema(schemaGenerator.generateOutputSchema(metadata));
        content201.addMediaType("application/json", mediaType201);
        response201.setContent(content201);
        responses.addApiResponse("201", response201);

        // 400 Bad Request
        ApiResponse response400 = new ApiResponse();
        response400.setDescription("Invalid input data / Validation error");
        responses.addApiResponse("400", response400);

        operation.setResponses(responses);

        return operation;
    }

    /**
     * Build update operation (PUT /{id}).
     *
     * @param metadata the entity metadata
     * @param tags     the operation tags
     * @return configured Operation
     */
    public Operation buildUpdateOperation(EntityMetadata metadata, String[] tags) {
        log.debug("Building update operation for {}", metadata.entityName());

        Operation operation = new Operation();
        operation.setSummary("Update an existing " + metadata.entityName());
        operation.setDescription("Update an existing " + metadata.entityName() + " entity with the provided data. The entire entity is replaced.");
        operation.setTags(Arrays.asList(tags));
        operation.setOperationId("update" + metadata.entityName());

        // Add ID path parameter
        operation.addParametersItem(createIdPathParameter(metadata));

        // Add request body
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription("Updated " + metadata.entityName() + " data");
        requestBody.setRequired(true);
        Content requestContent = new Content();
        MediaType requestMediaType = new MediaType();
        requestMediaType.setSchema(schemaGenerator.generateInputSchema(metadata));
        requestContent.addMediaType("application/json", requestMediaType);
        requestBody.setContent(requestContent);
        operation.setRequestBody(requestBody);

        // Add responses
        ApiResponses responses = new ApiResponses();

        // 200 OK
        ApiResponse response200 = new ApiResponse();
        response200.setDescription("Entity successfully updated");
        Content content200 = new Content();
        MediaType mediaType200 = new MediaType();
        mediaType200.setSchema(schemaGenerator.generateOutputSchema(metadata));
        content200.addMediaType("application/json", mediaType200);
        response200.setContent(content200);
        responses.addApiResponse("200", response200);

        // 404 Not Found
        ApiResponse response404 = new ApiResponse();
        response404.setDescription("Entity not found");
        responses.addApiResponse("404", response404);

        // 400 Bad Request
        ApiResponse response400 = new ApiResponse();
        response400.setDescription("Invalid input data / Validation error");
        responses.addApiResponse("400", response400);

        operation.setResponses(responses);

        return operation;
    }

    /**
     * Build delete operation (DELETE /{id}).
     *
     * @param metadata the entity metadata
     * @param tags     the operation tags
     * @return configured Operation
     */
    public Operation buildDeleteOperation(EntityMetadata metadata, String[] tags) {
        log.debug("Building delete operation for {}", metadata.entityName());

        Operation operation = new Operation();
        operation.setSummary("Delete a " + metadata.entityName());
        operation.setDescription("Delete a " + metadata.entityName() + " entity by its unique identifier.");
        operation.setTags(Arrays.asList(tags));
        operation.setOperationId("delete" + metadata.entityName());

        // Add ID path parameter
        operation.addParametersItem(createIdPathParameter(metadata));

        // Add responses
        ApiResponses responses = new ApiResponses();

        // 204 No Content
        ApiResponse response204 = new ApiResponse();
        response204.setDescription("Entity successfully deleted");
        responses.addApiResponse("204", response204);

        // 404 Not Found
        ApiResponse response404 = new ApiResponse();
        response404.setDescription("Entity not found");
        responses.addApiResponse("404", response404);

        operation.setResponses(responses);

        return operation;
    }

    /**
     * Create page query parameter.
     */
    private Parameter createPageParameter() {
        QueryParameter param = new QueryParameter();
        param.setName("page");
        param.setDescription("Page number (0-indexed)");
        param.setRequired(false);
        param.setSchema(new io.swagger.v3.oas.models.media.IntegerSchema()._default(0));
        return param;
    }

    /**
     * Create size query parameter.
     */
    private Parameter createSizeParameter() {
        QueryParameter param = new QueryParameter();
        param.setName("size");
        param.setDescription("Page size");
        param.setRequired(false);
        param.setSchema(new io.swagger.v3.oas.models.media.IntegerSchema()._default(20));
        return param;
    }

    /**
     * Create sort query parameter.
     */
    private Parameter createSortParameter() {
        QueryParameter param = new QueryParameter();
        param.setName("sort");
        param.setDescription("Sort criteria (e.g., 'name,asc' or 'createdDate,desc')");
        param.setRequired(false);
        param.setSchema(new io.swagger.v3.oas.models.media.StringSchema());
        return param;
    }

    /**
     * Create ID path parameter.
     */
    private Parameter createIdPathParameter(EntityMetadata metadata) {
        PathParameter param = new PathParameter();
        param.setName("id");
        param.setDescription(metadata.entityName() + " ID");
        param.setRequired(true);

        // Determine ID schema type based on metadata.idType()
        Schema<?> idSchema;
        Class<?> idType = metadata.idType();
        if (idType == Long.class || idType == long.class) {
            idSchema = new io.swagger.v3.oas.models.media.IntegerSchema().format("int64");
        } else if (idType == Integer.class || idType == int.class) {
            idSchema = new io.swagger.v3.oas.models.media.IntegerSchema().format("int32");
        } else {
            idSchema = new io.swagger.v3.oas.models.media.StringSchema();
        }

        param.setSchema(idSchema);
        return param;
    }

    /**
     * Create a page schema for paginated responses.
     */
    private Schema<?> createPageSchema(EntityMetadata metadata) {
        // Create a simplified page object schema
        io.swagger.v3.oas.models.media.ObjectSchema pageSchema = new io.swagger.v3.oas.models.media.ObjectSchema();
        pageSchema.addProperty("content", new io.swagger.v3.oas.models.media.ArraySchema()
                .items(schemaGenerator.generateOutputSchema(metadata)));
        pageSchema.addProperty("totalElements", new io.swagger.v3.oas.models.media.IntegerSchema().format("int64"));
        pageSchema.addProperty("totalPages", new io.swagger.v3.oas.models.media.IntegerSchema().format("int32"));
        pageSchema.addProperty("size", new io.swagger.v3.oas.models.media.IntegerSchema().format("int32"));
        pageSchema.addProperty("number", new io.swagger.v3.oas.models.media.IntegerSchema().format("int32"));
        return pageSchema;
    }
}
