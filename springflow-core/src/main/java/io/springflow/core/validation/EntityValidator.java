package io.springflow.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Entity validator that supports JSR-380 validation groups.
 * <p>
 * This component provides programmatic validation with group support,
 * allowing different validation rules for create vs update operations.
 * </p>
 * <p>
 * Validation groups used:
 * </p>
 * <ul>
 *   <li>{@link ValidationGroups.Create} - Applied on POST (create) operations</li>
 *   <li>{@link ValidationGroups.Update} - Applied on PUT/PATCH (update) operations</li>
 * </ul>
 *
 * @see ValidationGroups
 * @see jakarta.validation.Validator
 * @since 0.4.0
 */
public class EntityValidator {

    private static final Logger log = LoggerFactory.getLogger(EntityValidator.class);

    private final Validator validator;

    public EntityValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate an entity for creation (POST).
     * Applies {@link ValidationGroups.Create} group.
     *
     * @param entity the entity to validate
     * @param <T>    the entity type
     * @throws ConstraintViolationException if validation fails
     */
    public <T> void validateForCreate(T entity) {
        log.debug("Validating {} for creation with Create group", entity.getClass().getSimpleName());
        validate(entity, ValidationGroups.Create.class);
    }

    /**
     * Validate an entity for update (PUT/PATCH).
     * Applies {@link ValidationGroups.Update} group.
     *
     * @param entity the entity to validate
     * @param <T>    the entity type
     * @throws ConstraintViolationException if validation fails
     */
    public <T> void validateForUpdate(T entity) {
        log.debug("Validating {} for update with Update group", entity.getClass().getSimpleName());
        validate(entity, ValidationGroups.Update.class);
    }

    /**
     * Validate an entity with specific validation groups.
     *
     * @param entity the entity to validate
     * @param groups the validation groups to apply
     * @param <T>    the entity type
     * @throws ConstraintViolationException if validation fails
     */
    public <T> void validate(T entity, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity, groups);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for {}: {} violations",
                    entity.getClass().getSimpleName(), violations.size());
            throw new ConstraintViolationException(violations);
        }
        log.debug("Validation passed for {}", entity.getClass().getSimpleName());
    }

    /**
     * Validate a specific property of an entity.
     *
     * @param entity       the entity
     * @param propertyName the property name to validate
     * @param groups       the validation groups
     * @param <T>          the entity type
     * @throws ConstraintViolationException if validation fails
     */
    public <T> void validateProperty(T entity, String propertyName, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(entity, propertyName, groups);
        if (!violations.isEmpty()) {
            log.warn("Property validation failed for {}.{}: {} violations",
                    entity.getClass().getSimpleName(), propertyName, violations.size());
            throw new ConstraintViolationException(violations);
        }
    }
}
