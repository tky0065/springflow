package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables soft delete functionality for an entity.
 *
 * <p>When applied to an entity, DELETE operations will not physically remove
 * the record from the database. Instead, a flag is set to mark it as deleted.
 *
 * <p><b>Phase 2 Feature</b> - Implementation pending.
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Adds {@code deleted} boolean field (default false)</li>
 *   <li>Adds {@code deletedAt} timestamp field (nullable)</li>
 *   <li>DELETE endpoint marks record as deleted instead of removing it</li>
 *   <li>GET endpoints automatically filter out deleted records</li>
 *   <li>Adds restore endpoint: {@code POST /resource/{id}/restore}</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "users")
 * @SoftDelete
 * public class User {
 *     @Id
 *     private Long id;
 *
 *     private String username;
 *
 *     // These fields are added automatically:
 *     // private Boolean deleted = false;
 *     // private LocalDateTime deletedAt;
 * }
 * }</pre>
 *
 * <h3>Query Parameters:</h3>
 * <ul>
 *   <li>{@code ?includeDeleted=true} - Include deleted records in results</li>
 *   <li>{@code ?deletedOnly=true} - Show only deleted records</li>
 * </ul>
 *
 * @author SpringFlow
 * @since 0.2.0 (Phase 2)
 * @see AutoApi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoftDelete {

    /**
     * Name of the boolean field to track deletion status.
     *
     * <p>Defaults to "deleted".
     *
     * @return the field name for deletion flag
     */
    String deletedField() default "deleted";

    /**
     * Name of the timestamp field to track deletion time.
     *
     * <p>Defaults to "deletedAt".
     *
     * @return the field name for deletion timestamp
     */
    String deletedAtField() default "deletedAt";
}
