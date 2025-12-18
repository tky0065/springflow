package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables audit trail functionality for an entity.
 *
 * <p>Automatically tracks who created/modified a record and when.
 * Integrates with Spring Data JPA auditing.
 *
 * <p><b>Phase 2 Feature</b> - Implementation pending.
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Adds {@code createdAt} timestamp (set on creation)</li>
 *   <li>Adds {@code updatedAt} timestamp (updated on every change)</li>
 *   <li>Adds {@code createdBy} string (username who created)</li>
 *   <li>Adds {@code updatedBy} string (username who last modified)</li>
 *   <li>Optionally adds {@code version} for optimistic locking</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "documents")
 * @Auditable
 * public class Document {
 *     @Id
 *     private Long id;
 *
 *     private String title;
 *     private String content;
 *
 *     // These fields are added automatically:
 *     // private LocalDateTime createdAt;
 *     // private LocalDateTime updatedAt;
 *     // private String createdBy;
 *     // private String updatedBy;
 * }
 * }</pre>
 *
 * <h3>Integration:</h3>
 * <p>Requires Spring Security for user context. Falls back to "system"
 * if no authenticated user is available.
 *
 * @author SpringFlow
 * @since 0.2.0 (Phase 2)
 * @see AutoApi
 * @see SoftDelete
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Enable version field for optimistic locking.
     *
     * <p>When enabled, adds a {@code @Version} annotated Long field
     * to prevent concurrent modification conflicts.
     *
     * <p>Defaults to {@code false}.
     *
     * @return true to enable versioning
     */
    boolean versioned() default false;

    /**
     * Name of the created timestamp field.
     *
     * <p>Defaults to "createdAt".
     *
     * @return the field name for creation timestamp
     */
    String createdAtField() default "createdAt";

    /**
     * Name of the updated timestamp field.
     *
     * <p>Defaults to "updatedAt".
     *
     * @return the field name for update timestamp
     */
    String updatedAtField() default "updatedAt";

    /**
     * Name of the created by field.
     *
     * <p>Defaults to "createdBy".
     *
     * @return the field name for creator
     */
    String createdByField() default "createdBy";

    /**
     * Name of the updated by field.
     *
     * <p>Defaults to "updatedBy".
     *
     * @return the field name for last modifier
     */
    String updatedByField() default "updatedBy";
}
