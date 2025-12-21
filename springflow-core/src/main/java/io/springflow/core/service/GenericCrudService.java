package io.springflow.core.service;

import io.springflow.core.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD service providing common operations for entities.
 * <p>
 * This abstract class provides default implementations for standard CRUD operations
 * including transaction management, error handling, and logging.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
@Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.DEFAULT,
        rollbackFor = Exception.class
)
public abstract class GenericCrudService<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(GenericCrudService.class);

    protected final JpaRepository<T, ID> repository;
    protected final Class<T> entityClass;

    protected GenericCrudService(JpaRepository<T, ID> repository, Class<T> entityClass) {
        this.repository = repository;
        this.entityClass = entityClass;
    }

    /**
     * Find all entities with pagination support.
     *
     * @param pageable pagination information
     * @return a page of entities
     */
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        log.debug("Finding all {} with pagination: {}", entityClass.getSimpleName(), pageable);
        return repository.findAll(pageable);
    }

    /**
     * Find all entities.
     *
     * @return list of all entities
     */
    @Transactional(readOnly = true)
    public List<T> findAll() {
        log.debug("Finding all {}", entityClass.getSimpleName());
        return repository.findAll();
    }

    /**
     * Find all entities with specification and pagination support.
     * <p>
     * This method requires the repository to implement {@link JpaSpecificationExecutor}.
     * </p>
     *
     * @param spec     the specification to apply
     * @param pageable pagination information
     * @return a page of entities matching the specification
     * @throws UnsupportedOperationException if repository doesn't support specifications
     */
    @Transactional(readOnly = true)
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        log.debug("Finding all {} with specification and pagination", entityClass.getSimpleName());
        if (repository instanceof JpaSpecificationExecutor) {
            @SuppressWarnings("unchecked")
            JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
            return specExecutor.findAll(spec, pageable);
        }
        throw new UnsupportedOperationException(
                "Repository does not support JpaSpecificationExecutor for dynamic filtering"
        );
    }

    /**
     * Find an entity by its ID.
     *
     * @param id the entity ID
     * @return the entity
     * @throws EntityNotFoundException if entity not found
     */
    @Transactional(readOnly = true)
    public T findById(ID id) {
        log.debug("Finding {} with id: {}", entityClass.getSimpleName(), id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityClass, id));
    }

    /**
     * Find an entity by its ID, returning an Optional.
     *
     * @param id the entity ID
     * @return optional containing the entity if found
     */
    @Transactional(readOnly = true)
    public Optional<T> findByIdOptional(ID id) {
        log.debug("Finding {} with id (optional): {}", entityClass.getSimpleName(), id);
        return repository.findById(id);
    }

    /**
     * Check if an entity exists by its ID.
     *
     * @param id the entity ID
     * @return true if entity exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        log.debug("Checking if {} exists with id: {}", entityClass.getSimpleName(), id);
        return repository.existsById(id);
    }

    /**
     * Save a new entity.
     * <p>
     * Calls {@link #beforeCreate(Object)} hook before saving.
     * Calls {@link #afterCreate(Object)} hook after saving.
     * </p>
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    public T save(T entity) {
        log.debug("Saving new {}: {}", entityClass.getSimpleName(), entity);
        beforeCreate(entity);
        T saved = repository.save(entity);
        afterCreate(saved);
        log.info("Created new {} with result: {}", entityClass.getSimpleName(), saved);
        return saved;
    }

    /**
     * Update an existing entity.
     * <p>
     * Calls {@link #beforeUpdate(Object, Object)} hook before updating.
     * Calls {@link #afterUpdate(Object)} hook after updating.
     * </p>
     *
     * @param id     the entity ID
     * @param entity the entity with updated data
     * @return the updated entity
     * @throws EntityNotFoundException if entity not found
     */
    public T update(ID id, T entity) {
        log.debug("Updating {} with id: {}", entityClass.getSimpleName(), id);
        T existing = findById(id);
        beforeUpdate(existing, entity);
        T updated = repository.save(entity);
        afterUpdate(updated);
        log.info("Updated {} with id: {}", entityClass.getSimpleName(), id);
        return updated;
    }

    /**
     * Delete an entity by its ID.
     * <p>
     * Calls {@link #beforeDelete(Object)} hook before deletion.
     * Calls {@link #afterDelete(Object)} hook after deletion.
     * </p>
     *
     * @param id the entity ID
     * @throws EntityNotFoundException if entity not found
     */
    public void deleteById(ID id) {
        log.debug("Deleting {} with id: {}", entityClass.getSimpleName(), id);
        T entity = findById(id);
        beforeDelete(id);
        repository.deleteById(id);
        afterDelete(id);
        log.info("Deleted {} with id: {}", entityClass.getSimpleName(), id);
    }

    /**
     * Count all entities.
     *
     * @return total number of entities
     */
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting all {}", entityClass.getSimpleName());
        return repository.count();
    }

    // Business logic hooks (can be overridden by subclasses)

    /**
     * Hook called before creating a new entity.
     * Override this method to add custom business logic.
     *
     * @param entity the entity to be created
     */
    protected void beforeCreate(T entity) {
        // Default: no-op
    }

    /**
     * Hook called after creating a new entity.
     * Override this method to add custom business logic.
     *
     * @param entity the created entity
     */
    protected void afterCreate(T entity) {
        // Default: no-op
    }

    /**
     * Hook called before updating an entity.
     * Override this method to add custom business logic.
     *
     * @param existing the existing entity
     * @param updated  the entity with updated data
     */
    protected void beforeUpdate(T existing, T updated) {
        // Default: no-op
    }

    /**
     * Hook called after updating an entity.
     * Override this method to add custom business logic.
     *
     * @param entity the updated entity
     */
    protected void afterUpdate(T entity) {
        // Default: no-op
    }

    /**
     * Hook called before deleting an entity.
     * Override this method to add custom business logic.
     *
     * @param id the ID of the entity to be deleted
     */
    protected void beforeDelete(ID id) {
        // Default: no-op
    }

    /**
     * Hook called after deleting an entity.
     * Override this method to add custom business logic.
     *
     * @param id the ID of the deleted entity
     */
    protected void afterDelete(ID id) {
        // Default: no-op
    }
}
