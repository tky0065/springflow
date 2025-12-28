package io.springflow.graphql.controller;

import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.GenericCrudService;
import io.springflow.graphql.filter.GraphQLFilterConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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
    protected final FilterResolver filterResolver;
    protected final GraphQLFilterConverter filterConverter;
    protected final EntityMetadata metadata;
    protected final String entityName;
    protected final String pluralName;

    @SuppressWarnings("unchecked")
    protected GenericGraphQLController(GenericCrudService<T, ID> service,
                                      DtoMapperFactory dtoMapperFactory,
                                      FilterResolver filterResolver,
                                      GraphQLFilterConverter filterConverter,
                                      EntityMetadata metadata) {
        this.service = service;
        this.dtoMapper = (DtoMapper<T, ID>) dtoMapperFactory.getMapper(metadata.entityClass(), metadata);
        this.filterResolver = filterResolver;
        this.filterConverter = filterConverter;
        this.metadata = metadata;
        this.entityName = metadata.entityName();
        this.pluralName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "s";
    }

    /**
     * Query: Find all entities with pagination and filtering.
     * <p>
     * GraphQL query example:
     * <pre>
     * query {
     *   products(
     *     page: 0,
     *     size: 10,
     *     filters: {
     *       name_like: "Laptop"
     *       price_gte: "500"
     *       price_lte: "2000"
     *     }
     *   ) {
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
     * @param page    page number (default: 0)
     * @param size    page size (default: 20)
     * @param filters optional filter criteria (map-based for simplicity)
     * @return paginated result with content and pageInfo
     */
    // @QueryMapping annotation will be added dynamically by ByteBuddy for each entity
    public Map<String, Object> findAll(@Argument Integer page,
                                        @Argument Integer size,
                                        @Argument Map<String, Object> filters) {
        // Set default values if not provided
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;

        log.debug("GraphQL Query: {}(page={}, size={}, filters={})", pluralName, pageNumber, pageSize, filters);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        // Apply filters if provided
        Page<T> entityPage;
        if (filters != null && !filters.isEmpty()) {
            Map<String, String> filterParams = filterConverter.convertSimpleFilter(filters);
            Specification<T> spec = filterResolver.buildSpecification(filterParams, metadata, null);
            entityPage = service.findAll(spec, pageRequest);
        } else {
            entityPage = service.findAll(pageRequest);
        }

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
    // @QueryMapping annotation will be added dynamically by ByteBuddy for each entity
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
    // @MutationMapping annotation will be added dynamically by ByteBuddy for each entity
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
    // @MutationMapping annotation will be added dynamically by ByteBuddy for each entity
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
    // @MutationMapping annotation will be added dynamically by ByteBuddy for each entity
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
