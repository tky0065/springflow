package io.springflow.graphql.controller;

import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.GenericCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic GraphQL controller that provides queries and mutations for an entity.
 * <p>
 * This controller is dynamically instantiated for each entity annotated with @AutoApi
 * and provides standard CRUD operations via GraphQL.
 * </p>
 *
 * <p>Generated queries:</p>
 * <ul>
 *   <li>{@code entities(page, size)}: Paginated list</li>
 *   <li>{@code entity(id)}: Single entity by ID</li>
 * </ul>
 *
 * <p>Generated mutations:</p>
 * <ul>
 *   <li>{@code createEntity(input)}: Create new entity</li>
 *   <li>{@code updateEntity(id, input)}: Update existing entity</li>
 *   <li>{@code deleteEntity(id)}: Delete entity</li>
 * </ul>
 *
 * @param <T>  the entity type
 * @param <ID> the ID type
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
public abstract class GenericGraphQLController<T, ID> {

    protected final GenericCrudService<T, ID> service;
    protected final DtoMapper<T, ID> dtoMapper;
    protected final EntityMetadata metadata;
    protected final String entityName;
    protected final String pluralName;

    protected GenericGraphQLController(GenericCrudService<T, ID> service,
                                      DtoMapper<T, ID> dtoMapper,
                                      EntityMetadata metadata) {
        this.service = service;
        this.dtoMapper = dtoMapper;
        this.metadata = metadata;
        this.entityName = metadata.entityName();
        this.pluralName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "s";
    }

    /**
     * Query: Find all entities with pagination.
     * <p>
     * GraphQL query example:
     * <pre>
     * query {
     *   products(page: 0, size: 10) {
     *     content {
     *       id
     *       name
     *       price
     *     }
     *     pageInfo {
     *       totalElements
     *       totalPages
     *     }
     *   }
     * }
     * </pre>
     *
     * @param page page number (default: 0)
     * @param size page size (default: 20)
     * @return paginated result with content and pageInfo
     */
    @QueryMapping(name = "#{target.pluralName}")
    public Map<String, Object> findAll(@Argument Integer page,
                                        @Argument Integer size) {
        // Set default values if not provided
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;

        log.debug("GraphQL Query: {}(page={}, size={})", pluralName, pageNumber, pageSize);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<T> entityPage = service.findAll(pageRequest);
        Page<Map<String, Object>> dtoPage = dtoMapper.toOutputDtoPage(entityPage, null);

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoPage.getContent());
        response.put("pageInfo", buildPageInfo(dtoPage));

        log.debug("GraphQL Query {} returned {} items", pluralName, dtoPage.getNumberOfElements());
        return response;
    }

    /**
     * Query: Find entity by ID.
     * <p>
     * GraphQL query example:
     * <pre>
     * query {
     *   product(id: "1") {
     *     id
     *     name
     *     price
     *   }
     * }
     * </pre>
     *
     * @param id the entity ID
     * @return entity DTO or null if not found
     */
    @QueryMapping(name = "#{target.entityName.toLowerCase()}")
    public Map<String, Object> findById(@Argument ID id) {
        log.debug("GraphQL Query: {}(id={})", entityName.toLowerCase(), id);
        T entity = service.findById(id);
        return dtoMapper.toOutputDto(entity, null);
    }

    /**
     * Mutation: Create a new entity.
     * <p>
     * GraphQL mutation example:
     * <pre>
     * mutation {
     *   createProduct(input: { name: "Laptop", price: 999.99 }) {
     *     id
     *     name
     *     price
     *   }
     * }
     * </pre>
     *
     * @param input entity data as Map
     * @return created entity DTO
     */
    @MutationMapping(name = "create#{target.entityName}")
    public Map<String, Object> create(@Argument Map<String, Object> input) {
        log.debug("GraphQL Mutation: create{}(input={})", entityName, input);
        T entity = dtoMapper.toEntity(input);
        T created = service.save(entity);
        return dtoMapper.toOutputDto(created, null);
    }

    /**
     * Mutation: Update an existing entity.
     * <p>
     * GraphQL mutation example:
     * <pre>
     * mutation {
     *   updateProduct(id: "1", input: { name: "Gaming Laptop", price: 1499.99 }) {
     *     id
     *     name
     *     price
     *   }
     * }
     * </pre>
     *
     * @param id    entity ID
     * @param input updated entity data as Map
     * @return updated entity DTO
     */
    @MutationMapping(name = "update#{target.entityName}")
    public Map<String, Object> update(@Argument ID id, @Argument Map<String, Object> input) {
        log.debug("GraphQL Mutation: update{}(id={}, input={})", entityName, id, input);
        T existing = service.findById(id);
        dtoMapper.updateEntity(existing, input);
        T updated = service.save(existing);
        return dtoMapper.toOutputDto(updated, null);
    }

    /**
     * Mutation: Delete an entity.
     * <p>
     * GraphQL mutation example:
     * <pre>
     * mutation {
     *   deleteProduct(id: "1")
     * }
     * </pre>
     *
     * @param id entity ID
     * @return true if deleted successfully
     */
    @MutationMapping(name = "delete#{target.entityName}")
    public boolean delete(@Argument ID id) {
        log.debug("GraphQL Mutation: delete{}(id={})", entityName, id);
        service.deleteById(id);
        return true;
    }

    /**
     * Builds PageInfo object from Spring Data Page.
     */
    private Map<String, Object> buildPageInfo(Page<?> page) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", page.getNumber());
        pageInfo.put("pageSize", page.getSize());
        pageInfo.put("totalElements", (int) page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("hasNext", page.hasNext());
        pageInfo.put("hasPrevious", page.hasPrevious());
        return pageInfo;
    }

    /**
     * Gets the entity name (used by SpEL in @QueryMapping/@MutationMapping).
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Gets the plural entity name (used by SpEL in @QueryMapping).
     */
    public String getPluralName() {
        return pluralName;
    }
}
