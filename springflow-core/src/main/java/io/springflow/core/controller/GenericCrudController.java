package io.springflow.core.controller;

import io.springflow.core.service.GenericCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Generic CRUD controller providing RESTful endpoints for entities.
 * <p>
 * This abstract controller provides standard REST operations (GET, POST, PUT, DELETE)
 * with proper HTTP status codes, validation, and error handling.
 * </p>
 * <p>
 * Note: For Phase 1 MVP, this controller works directly with entities.
 * DTO mapping will be added in Module 12.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public abstract class GenericCrudController<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(GenericCrudController.class);

    protected final GenericCrudService<T, ID> service;
    protected final Class<T> entityClass;

    protected GenericCrudController(GenericCrudService<T, ID> service, Class<T> entityClass) {
        this.service = service;
        this.entityClass = entityClass;
    }

    /**
     * GET / - Find all entities with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return page of entities with HTTP 200 OK
     */
    @Operation(
            summary = "List all entities",
            description = "Retrieve a paginated list of all entities. Supports pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<Page<T>> findAll(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("GET request to find all {} with pagination: {}", entityClass.getSimpleName(), pageable);
        Page<T> page = service.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /{id} - Find entity by ID.
     *
     * @param id the entity ID
     * @return entity with HTTP 200 OK, or HTTP 404 NOT FOUND if not exists
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
    public ResponseEntity<T> findById(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id) {
        log.debug("GET request to find {} with id: {}", entityClass.getSimpleName(), id);
        T entity = service.findById(id);
        return ResponseEntity.ok(entity);
    }

    /**
     * POST / - Create a new entity.
     *
     * @param entity the entity to create (validated)
     * @return created entity with HTTP 201 CREATED and Location header
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
    public ResponseEntity<T> create(
            @Parameter(description = "Entity to create", required = true)
            @Valid @RequestBody T entity) {
        log.debug("POST request to create new {}: {}", entityClass.getSimpleName(), entity);
        T created = service.save(entity);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(getEntityId(created))
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * PUT /{id} - Update an existing entity (full update).
     *
     * @param id     the entity ID
     * @param entity the entity with updated data (validated)
     * @return updated entity with HTTP 200 OK, or HTTP 404 NOT FOUND if not exists
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
    public ResponseEntity<T> update(
            @Parameter(description = "Entity ID", required = true)
            @PathVariable ID id,
            @Parameter(description = "Updated entity data", required = true)
            @Valid @RequestBody T entity) {
        log.debug("PUT request to update {} with id: {}", entityClass.getSimpleName(), id);
        T updated = service.update(id, entity);
        return ResponseEntity.ok(updated);
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
     * Extract the ID from an entity.
     * Subclasses can override this method for custom ID extraction logic.
     *
     * @param entity the entity
     * @return the entity ID
     */
    protected abstract ID getEntityId(T entity);
}
