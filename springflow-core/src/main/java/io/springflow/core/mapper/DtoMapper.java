package io.springflow.core.mapper;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Generic DTO mapper interface for entity-DTO conversions.
 * <p>
 * For Phase 1 MVP, this interface uses Map-based DTOs to avoid runtime class generation complexity.
 * InputDTO is represented as {@code Map<String, Object>} and OutputDTO uses the entity directly
 * with selective field filtering.
 * </p>
 * <p>
 * Future versions may use actual DTO classes generated via ByteBuddy or MapStruct.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public interface DtoMapper<T, ID> {

    /**
     * Converts a Map-based InputDTO to an entity.
     * <p>
     * Excludes ID field (auto-generated) and @Hidden fields.
     * Copies validation constraints from entity metadata.
     * </p>
     *
     * @param inputDto the input data as a Map
     * @return the entity instance
     * @throws IllegalArgumentException if validation fails or field types mismatch
     */
    T toEntity(Map<String, Object> inputDto);

    /**
     * Converts an entity to a Map-based OutputDTO.
     * <p>
     * Includes ID field and excludes @Hidden fields.
     * Handles @ReadOnly fields appropriately.
     * </p>
     *
     * @param entity the entity
     * @return the output data as a Map
     */
    Map<String, Object> toOutputDto(T entity);

    /**
     * Updates an existing entity with data from InputDTO.
     * <p>
     * Preserves ID and other immutable fields.
     * Excludes @ReadOnly fields from update.
     * </p>
     *
     * @param entity   the existing entity to update
     * @param inputDto the new data as a Map
     */
    void updateEntity(T entity, Map<String, Object> inputDto);

    /**
     * Converts a list of entities to OutputDTOs.
     *
     * @param entities the list of entities
     * @return the list of output DTOs
     */
    List<Map<String, Object>> toOutputDtoList(List<T> entities);

    /**
     * Converts a page of entities to a page of OutputDTOs.
     *
     * @param entityPage the page of entities
     * @return the page of output DTOs
     */
    Page<Map<String, Object>> toOutputDtoPage(Page<T> entityPage);

    /**
     * Gets the entity class this mapper handles.
     *
     * @return the entity class
     */
    Class<T> getEntityClass();
}
