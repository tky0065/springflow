package io.springflow.core.controller;

import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.service.GenericCrudService;
import io.springflow.core.utils.EntityUtils;
import io.springflow.core.validation.EntityValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Generic CRUD controller providing RESTful endpoints for entities.
 * <p>
 * This abstract controller provides standard REST operations (GET, POST, PUT, DELETE)
 * with proper HTTP status codes, validation, and error handling.
 * </p>
 * <p>
 * Uses Map-based DTOs for input/output to respect @Hidden and @ReadOnly annotations.
 * DtoMapper handles conversion between entities and DTOs.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public abstract class GenericCrudController<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(GenericCrudController.class);

    @Autowired
    protected ApplicationContext applicationContext;

    protected GenericCrudService<T, ID> service;
    
    @Autowired
    protected DtoMapperFactory dtoMapperFactory;
    
    @Autowired
    protected FilterResolver filterResolver;
    
    @Autowired
    protected EntityValidator entityValidator;

    protected DtoMapper<T, ID> dtoMapper;
    protected EntityMetadata metadata;
    protected Class<T> entityClass;

    /**
     * Default constructor for subclasses.
     * Dependencies will be injected via @Autowired and initialized in @PostConstruct.
     */
    @SuppressWarnings("unchecked")
    protected GenericCrudController() {
        try {
            // Resolve entityClass from generic parameter T
            this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
        } catch (Exception e) {
            log.debug("Could not automatically resolve entity class for {}, manual initialization required", 
                    getClass().getSimpleName());
        }
    }

    protected GenericCrudController(GenericCrudService<T, ID> service,
                                    DtoMapper<T, ID> dtoMapper,
                                    FilterResolver filterResolver,
                                    EntityMetadata metadata,
                                    Class<T> entityClass,
                                    EntityValidator entityValidator) {
        this.service = service;
        this.dtoMapper = dtoMapper;
        this.filterResolver = filterResolver;
        this.metadata = metadata;
        this.entityClass = entityClass;
        this.entityValidator = entityValidator;
    }

    /**
     * Constructor without EntityValidator for backward compatibility.
     * Validation groups will not be applied if this constructor is used.
     *
     * @deprecated Use constructor with EntityValidator parameter
     */
    @Deprecated
    protected GenericCrudController(GenericCrudService<T, ID> service,
                                    DtoMapper<T, ID> dtoMapper,
                                    FilterResolver filterResolver,
                                    EntityMetadata metadata,
                                    Class<T> entityClass) {
        this(service, dtoMapper, filterResolver, metadata, entityClass, null);
    }

    /**
     * Initializes metadata and DtoMapper if not already provided via constructor.
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void init() {
        if (this.metadata == null && this.entityClass != null) {
            this.metadata = new MetadataResolver().resolve(this.entityClass);
        }

        // Resolve service by name convention if not provided
        if (this.service == null && this.entityClass != null && applicationContext != null) {
            String serviceBeanName = StringUtils.uncapitalize(this.entityClass.getSimpleName()) + "Service";
            if (applicationContext.containsBean(serviceBeanName)) {
                this.service = (GenericCrudService<T, ID>) applicationContext.getBean(serviceBeanName);
            } else {
                log.debug("Service bean {} not found for {}", serviceBeanName, getClass().getSimpleName());
            }
        }

        if (this.dtoMapper == null && this.dtoMapperFactory != null && this.entityClass != null && this.metadata != null) {
            this.dtoMapper = dtoMapperFactory.getMapper(this.entityClass, this.metadata);
        }
    }

    /**
     * GET / - Find all entities with pagination and filtering.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @param params   query parameters for dynamic filtering
     * @return page of DTOs with HTTP 200 OK (excludes @Hidden fields)
     */
    @Operation(
            summary = "List all entities",
            description = "Retrieve a paginated list of all entities. Supports pagination, sorting, and dynamic filtering."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<PageResponse<Map<String, Object>>> findAll(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable,
            jakarta.servlet.http.HttpServletRequest request) {
        // Validate sort parameters before processing
        validateSort(pageable);

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.debug("GET request to find all {} with pagination: {} and parameters: {}",
                entityClass.getSimpleName(), pageable, parameterMap.keySet());

        boolean includeDeleted = Boolean.parseBoolean(getFirstParam(parameterMap, "includeDeleted", "false"));
        boolean deletedOnly = Boolean.parseBoolean(getFirstParam(parameterMap, "deletedOnly", "false"));
        List<String> fields = extractFields(getFirstParam(parameterMap, "fields", null));

        Specification<T> spec = filterResolver.buildSpecification(parameterMap, metadata, fields);
        Page<T> page;

        if (deletedOnly) {
             page = service.findDeletedOnly(spec, pageable);
        } else {
             page = service.findAll(spec, pageable, includeDeleted);
        }

        Page<Map<String, Object>> dtoPage = dtoMapper.toOutputDtoPage(page, fields);
        PageResponse<Map<String, Object>> response = new PageResponse<>(dtoPage);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /search - Advanced search using JPA Specifications.
     *
     * @param searchRequest the search request containing criteria
     * @param pageable      pagination parameters
     * @return page of DTOs matching the criteria
     */
    @Operation(
            summary = "Advanced search",
            description = "Perform an advanced search using a structured list of filter criteria. Supports complex logical operations."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved search results",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/search")
    public ResponseEntity<PageResponse<Map<String, Object>>> search(
            @Parameter(description = "Search criteria", required = true)
            @RequestBody io.springflow.core.dto.SearchRequest searchRequest,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable,
            jakarta.servlet.http.HttpServletRequest request) {
        // Validate sort parameters before processing
        validateSort(pageable);

        log.debug("POST request to search {} with request: {} and pagination: {}",
                entityClass.getSimpleName(), searchRequest, pageable);

        Map<String, String[]> parameterMap = request.getParameterMap();
        List<String> fields = extractFields(getFirstParam(parameterMap, "fields", null));

        Page<T> page = service.search(searchRequest, pageable);
        Page<Map<String, Object>> dtoPage = dtoMapper.toOutputDtoPage(page, fields);
        PageResponse<Map<String, Object>> response = new PageResponse<>(dtoPage);
        return ResponseEntity.ok(response);
    }

    private void validateSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return;
        }
        for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (metadata.getFieldByName(property).isEmpty()) {
                throw new IllegalArgumentException("Invalid sort field: " + property);
            }
        }
    }

    private String getFirstParam(Map<String, String[]> parameterMap, String key, String defaultValue) {
        String[] values = parameterMap.get(key);
        return (values != null && values.length > 0) ? values[0] : defaultValue;
    }

    /**
     * GET /{id} - Find entity by ID.
     *
     * @param id the entity ID
     * @param params query parameters (e.g., fields)
     * @return DTO with HTTP 200 OK (excludes @Hidden fields), or HTTP 404 NOT FOUND if not exists
     */
    @Operation(
            summary = "Get entity by ID",
            description = "Retrieve a single entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved entity",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id,
            jakarta.servlet.http.HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.debug("GET request to find {} with id: {}", entityClass.getSimpleName(), id);
        List<String> fields = extractFields(getFirstParam(parameterMap, "fields", null));
        
        Specification<T> spec = filterResolver.buildSpecification(parameterMap, metadata, fields);
        T entity = service.findById(id, spec);
        Map<String, Object> dto = dtoMapper.toOutputDto(entity, fields);
        return ResponseEntity.ok(dto);
    }

    private List<String> extractFields(String fieldsParam) {
        if (fieldsParam == null || fieldsParam.isBlank()) {
            return null;
        }
        return Arrays.asList(fieldsParam.split(","));
    }

    /**
     * POST / - Create a new entity.
     *
     * @param inputDto the input data as Map (excludes ID, @Hidden, @ReadOnly fields)
     * @return created entity DTO with HTTP 201 CREATED and Location header
     */
    @Operation(
            summary = "Create a new entity",
            description = "Create a new entity with the provided data. The request body is validated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Entity successfully created",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data / Validation error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @Parameter(description = "Entity data to create", required = true)
            @RequestBody Map<String, Object> inputDto) {
        log.debug("POST request to create new {}: {}", entityClass.getSimpleName(), inputDto);

        T entity = dtoMapper.toEntity(inputDto);

        // Validate with Create group if validator is available
        if (entityValidator != null) {
            entityValidator.validateForCreate(entity);
        }

        T created = service.save(entity);
        Map<String, Object> outputDto = dtoMapper.toOutputDto(created);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(getEntityId(created))
                .toUri();

        return ResponseEntity.created(location).body(outputDto);
    }

    /**
     * PUT /{id} - Update an existing entity (full update).
     *
     * @param id       the entity ID
     * @param inputDto the updated data as Map (excludes ID, @Hidden, @ReadOnly fields)
     * @return updated entity DTO with HTTP 200 OK, or HTTP 404 NOT FOUND if not exists
     */
    @Operation(
            summary = "Update an existing entity",
            description = "Update an existing entity with the provided data. The entire entity is replaced."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entity successfully updated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data / Validation error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id,
            @Parameter(description = "Updated entity data", required = true)
            @RequestBody Map<String, Object> inputDto) {
        log.debug("PUT request to update {} with id: {}", entityClass.getSimpleName(), id);

        T existing = service.findById(id);
        dtoMapper.updateEntity(existing, inputDto);

        // Validate with Update group if validator is available
        if (entityValidator != null) {
            entityValidator.validateForUpdate(existing);
        }

        T updated = service.save(existing);
        Map<String, Object> outputDto = dtoMapper.toOutputDto(updated);

        return ResponseEntity.ok(outputDto);
    }

    /**
     * PATCH /{id} - Partially update an existing entity.
     * <p>
     * Only the fields present in the request body will be updated.
     * Missing fields will remain unchanged. This is different from PUT which replaces the entire entity.
     * </p>
     *
     * @param id       the entity ID
     * @param inputDto the partial data as Map (only fields to update)
     * @return updated entity DTO with HTTP 200 OK, or HTTP 404 NOT FOUND if not exists
     */
    @Operation(
            summary = "Partially update an entity",
            description = "Update specific fields of an existing entity. Only the fields provided in the request body will be updated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entity successfully updated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid field names or validation error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> patch(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id,
            @Parameter(description = "Partial entity data (only fields to update)", required = true)
            @RequestBody Map<String, Object> inputDto) {
        log.debug("PATCH request to partially update {} with id: {} with data: {}",
                entityClass.getSimpleName(), id, inputDto);

        // Validate that provided fields are valid (not trying to update @Hidden or @ReadOnly fields)
        dtoMapper.validateUpdatableFields(inputDto);

        T existing = service.findById(id);
        dtoMapper.updateEntity(existing, inputDto);

        // Validate with Update group if validator is available
        if (entityValidator != null) {
            entityValidator.validateForUpdate(existing);
        }

        T updated = service.save(existing);
        Map<String, Object> outputDto = dtoMapper.toOutputDto(updated);

        return ResponseEntity.ok(outputDto);
    }



    /**
     * DELETE /{id} - Delete an entity.
     *
     * @param id the entity ID
     * @return HTTP 204 NO CONTENT on success, or HTTP 404 NOT FOUND if not exists
     */
    @Operation(
            summary = "Delete an entity",
            description = "Delete an entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Entity successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id) {
        log.debug("DELETE request to delete {} with id: {}", entityClass.getSimpleName(), id);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /{id}/restore - Restore a soft-deleted entity.
     *
     * @param id the entity ID
     * @return restored entity DTO with HTTP 200 OK
     */
    @Operation(
            summary = "Restore a soft-deleted entity",
            description = "Restore an entity that was previously soft-deleted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entity successfully restored"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Soft delete not enabled for this entity"
            )
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restore(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id) {
        log.debug("POST request to restore {} with id: {}", entityClass.getSimpleName(), id);
        T restored = service.restoreById(id);
        return ResponseEntity.ok(dtoMapper.toOutputDto(restored));
    }

    /**
     * DELETE /{id}/hard - Physical delete of an entity.
     *
     * @param id the entity ID
     * @return HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Hard delete an entity",
            description = "Permanently remove an entity from the database, even if soft delete is enabled."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Entity successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entity not found"
            )
    })
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id) {
        log.debug("DELETE request to hard delete {} with id: {}", entityClass.getSimpleName(), id);
        service.hardDeleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extract the ID from an entity.
     * Subclasses can override this method for custom ID extraction logic.
     *
     * @param entity the entity
     * @return the entity ID
     */
    protected ID getEntityId(T entity) {
        return EntityUtils.getEntityId(entity, this.metadata);
    }
}
