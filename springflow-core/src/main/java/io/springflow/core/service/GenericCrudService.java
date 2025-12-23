package io.springflow.core.service;

import io.springflow.core.exception.EntityNotFoundException;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.security.SecurityUtils;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
    protected final EntityMetadata metadata;

    protected GenericCrudService(JpaRepository<T, ID> repository, Class<T> entityClass, EntityMetadata metadata) {
        this.repository = repository;
        this.entityClass = entityClass;
        this.metadata = metadata;
    }

    protected GenericCrudService(JpaRepository<T, ID> repository, Class<T> entityClass) {
        this(repository, entityClass, null);
    }

    /**
     * Find all entities with pagination support.
     * <p>Filters out soft-deleted records if enabled and includeDeleted is false.</p>
     *
     * @param pageable pagination information
     * @param includeDeleted whether to include soft-deleted records
     * @return a page of entities
     */
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable, boolean includeDeleted) {
        log.debug("Finding all {} with pagination: {}, includeDeleted: {}", entityClass.getSimpleName(), pageable, includeDeleted);
        if (metadata != null && metadata.isSoftDeleteEnabled()) {
            return findAll(null, pageable, includeDeleted);
        }
        return repository.findAll(pageable);
    }

    /**
     * Find all entities.
     * <p>Filters out soft-deleted records if enabled and includeDeleted is false.</p>
     *
     * @param includeDeleted whether to include soft-deleted records
     * @return list of all entities
     */
    @Transactional(readOnly = true)
    public List<T> findAll(boolean includeDeleted) {
        log.debug("Finding all {}, includeDeleted: {}", entityClass.getSimpleName(), includeDeleted);
        if (metadata != null && metadata.isSoftDeleteEnabled() && repository instanceof JpaSpecificationExecutor) {
            return ((JpaSpecificationExecutor<T>) repository).findAll(buildSoftDeleteSpecification(includeDeleted));
        }
        return repository.findAll();
    }

    /**
     * Find all entities with specification and pagination support.
     * <p>Automatically adds soft-delete filter if enabled and includeDeleted is false.</p>
     *
     * @param spec     the specification to apply
     * @param pageable pagination information
     * @param includeDeleted whether to include soft-deleted records
     * @return a page of entities matching the specification
     * @throws UnsupportedOperationException if repository doesn't support specifications
     */
    @Transactional(readOnly = true)
    public Page<T> findAll(Specification<T> spec, Pageable pageable, boolean includeDeleted) {
        log.debug("Finding all {} with specification and pagination, includeDeleted: {}", 
                entityClass.getSimpleName(), includeDeleted);
        
        Specification<T> effectiveSpec = spec;
        if (metadata != null && metadata.isSoftDeleteEnabled()) {
            Specification<T> softDeleteSpec = buildSoftDeleteSpecification(includeDeleted);
            effectiveSpec = spec == null ? softDeleteSpec : spec.and(softDeleteSpec);
        }

        if (repository instanceof JpaSpecificationExecutor) {
            @SuppressWarnings("unchecked")
            JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
            return specExecutor.findAll(effectiveSpec, pageable);
        }
        
        if (spec == null && (metadata == null || !metadata.isSoftDeleteEnabled())) {
            return repository.findAll(pageable);
        }

        throw new UnsupportedOperationException(
                "Repository does not support JpaSpecificationExecutor for dynamic filtering"
        );
    }

    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return findAll(pageable, false);
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return findAll(false);
    }

    @Transactional(readOnly = true)
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return findAll(spec, pageable, false);
    }

    /**
     * Find only soft-deleted entities with pagination support.
     *
     * @param pageable pagination information
     * @return a page of soft-deleted entities
     */
    @Transactional(readOnly = true)
    public Page<T> findDeletedOnly(Pageable pageable) {
        return findDeletedOnly(null, pageable);
    }

    /**
     * Find only soft-deleted entities with specification and pagination support.
     *
     * @param spec     the specification to apply
     * @param pageable pagination information
     * @return a page of soft-deleted entities
     */
    @Transactional(readOnly = true)
    public Page<T> findDeletedOnly(Specification<T> spec, Pageable pageable) {
        log.debug("Finding all deleted {} with specification and pagination", entityClass.getSimpleName());
        
        if (metadata == null || !metadata.isSoftDeleteEnabled()) {
             log.warn("Soft delete not enabled for {}, returning empty page for findDeletedOnly", entityClass.getSimpleName());
             return Page.empty(pageable);
        }

        Specification<T> softDeleteSpec = buildSoftDeleteSpecification(false, true);
        Specification<T> effectiveSpec = spec == null ? softDeleteSpec : spec.and(softDeleteSpec);

        if (repository instanceof JpaSpecificationExecutor) {
            @SuppressWarnings("unchecked")
            JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
            return specExecutor.findAll(effectiveSpec, pageable);
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
        return findById(id, null);
    }

    /**
     * Find an entity by its ID and a specification (for fetch joins).
     *
     * @param id   the entity ID
     * @param spec the specification to apply
     * @return the entity
     * @throws EntityNotFoundException if entity not found
     */
    @Transactional(readOnly = true)
    public T findById(ID id, Specification<T> spec) {
        log.debug("Finding {} with id: {} and specification", entityClass.getSimpleName(), id);
        
        if (repository instanceof JpaSpecificationExecutor && metadata != null) {
            String idFieldName = metadata.getIdField().map(FieldMetadata::name).orElse("id");
            Specification<T> idSpec = (root, query, cb) -> cb.equal(root.get(idFieldName), id);
            Specification<T> effectiveSpec = spec == null ? idSpec : spec.and(idSpec);
            
            @SuppressWarnings("unchecked")
            JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
            return specExecutor.findOne(effectiveSpec)
                    .orElseThrow(() -> new EntityNotFoundException(entityClass, id));
        }

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
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    public T save(T entity) {
        log.debug("Saving new {}: {}", entityClass.getSimpleName(), entity);
        
        if (metadata != null && metadata.isAuditable()) {
            handleAuditing(entity, true);
        }
        
        beforeCreate(entity);
        T saved = repository.save(entity);
        afterCreate(saved);
        log.info("Created new {} with result: {}", entityClass.getSimpleName(), saved);
        return saved;
    }

    /**
     * Update an existing entity.
     *
     * @param id     the entity ID
     * @param entity the entity with updated data
     * @return the updated entity
     */
    public T update(ID id, T entity) {
        log.debug("Updating {} with id: {}", entityClass.getSimpleName(), id);
        T existing = findById(id);
        
        if (metadata != null && metadata.isAuditable()) {
            handleAuditing(entity, false);
        }
        
        beforeUpdate(existing, entity);
        T updated = repository.save(entity);
        afterUpdate(updated);
        log.info("Updated {} with id: {}", entityClass.getSimpleName(), id);
        return updated;
    }

    /**
     * Delete an entity by its ID.
     * <p>Performs a soft delete if {@code @SoftDelete} is present on the entity.</p>
     *
     * @param id the entity ID
     * @throws EntityNotFoundException if entity not found
     */
    public void deleteById(ID id) {
        log.debug("Deleting {} with id: {}", entityClass.getSimpleName(), id);
        T entity = findById(id);
        beforeDelete(id);

        if (metadata != null && metadata.isSoftDeleteEnabled()) {
            performSoftDelete(entity);
        } else {
            repository.deleteById(id);
        }

        afterDelete(id);
        log.info("Deleted {} with id: {}", entityClass.getSimpleName(), id);
    }

    /**
     * Performs a physical delete even if soft delete is enabled.
     *
     * @param id the entity ID
     */
    public void hardDeleteById(ID id) {
        log.debug("Hard deleting {} with id: {}", entityClass.getSimpleName(), id);
        if (!existsById(id)) {
            throw new EntityNotFoundException(entityClass, id);
        }
        repository.deleteById(id);
    }

    /**
     * Restores a soft-deleted entity.
     *
     * @param id the entity ID
     * @return the restored entity
     */
    public T restoreById(ID id) {
        log.debug("Restoring {} with id: {}", entityClass.getSimpleName(), id);
        if (metadata == null || !metadata.isSoftDeleteEnabled()) {
            throw new UnsupportedOperationException("Soft delete is not enabled for " + entityClass.getSimpleName());
        }

        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityClass, id));
        
        performRestore(entity);
        return repository.save(entity);
    }

    private void handleAuditing(T entity, boolean isCreate) {
        String currentUser = SecurityUtils.getCurrentUserLogin().orElse("system");
        LocalDateTime now = LocalDateTime.now();

        try {
            if (isCreate) {
                setFieldValueSafely(entity, metadata.auditableConfig().createdAtField(), now);
                setFieldValueSafely(entity, metadata.auditableConfig().createdByField(), currentUser);
            }
            setFieldValueSafely(entity, metadata.auditableConfig().updatedAtField(), now);
            setFieldValueSafely(entity, metadata.auditableConfig().updatedByField(), currentUser);
        } catch (Exception e) {
            log.warn("Failed to apply auditing to {}", entityClass.getSimpleName());
        }
    }

    private void setFieldValueSafely(T entity, String fieldName, Object value) {
        try {
            setFieldValue(entity, fieldName, value);
        } catch (Exception e) {
            // Ignore if field doesn't exist
        }
    }

    private void performSoftDelete(T entity) {
        String deletedField = metadata.softDeleteConfig().deletedField();
        String deletedAtField = metadata.softDeleteConfig().deletedAtField();

        try {
            setFieldValue(entity, deletedField, true);
            setFieldValue(entity, deletedAtField, LocalDateTime.now());
            repository.save(entity);
        } catch (Exception e) {
            log.error("Failed to perform soft delete on {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Soft delete failed", e);
        }
    }

    private void performRestore(T entity) {
        String deletedField = metadata.softDeleteConfig().deletedField();
        String deletedAtField = metadata.softDeleteConfig().deletedAtField();

        try {
            setFieldValue(entity, deletedField, false);
            setFieldValue(entity, deletedAtField, null);
        } catch (Exception e) {
            log.error("Failed to perform restore on {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Restore failed", e);
        }
    }

    private Specification<T> buildSoftDeleteSpecification(boolean includeDeleted) {
        return buildSoftDeleteSpecification(includeDeleted, false);
    }

    private Specification<T> buildSoftDeleteSpecification(boolean includeDeleted, boolean deletedOnly) {
        return (root, query, cb) -> {
            if (deletedOnly) {
                String deletedField = metadata.softDeleteConfig().deletedField();
                return cb.equal(root.get(deletedField), true);
            }
            if (includeDeleted) {
                return cb.conjunction();
            }
            String deletedField = metadata.softDeleteConfig().deletedField();
            return cb.or(
                cb.equal(root.get(deletedField), false),
                cb.isNull(root.get(deletedField))
            );
        };
    }

    private void setFieldValue(T entity, String fieldName, Object value) throws Exception {
        FieldMetadata fieldMeta = metadata.getFieldByName(fieldName).orElse(null);
        Field field;
        if (fieldMeta != null) {
            field = fieldMeta.field();
        } else {
            // Fallback to reflection if not in metadata
            field = entityClass.getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        field.set(entity, value);
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
