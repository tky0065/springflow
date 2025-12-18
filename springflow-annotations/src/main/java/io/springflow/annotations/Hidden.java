package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be excluded from auto-generated DTOs.
 *
 * <p>Fields annotated with {@code @Hidden} will not appear in:
 * <ul>
 *   <li>Input DTOs (POST/PUT request bodies)</li>
 *   <li>Output DTOs (GET response bodies)</li>
 *   <li>OpenAPI schema documentation</li>
 * </ul>
 *
 * <p>Useful for internal fields, computed values, or sensitive data
 * that should not be exposed via the REST API.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "users")
 * public class User {
 *     @Id
 *     private Long id;
 *
 *     private String username;
 *
 *     @Hidden
 *     private String passwordHash;
 *
 *     @Hidden
 *     private String internalNotes;
 * }
 * }</pre>
 *
 * <p>The generated DTOs will not include {@code passwordHash} or {@code internalNotes}.
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see ReadOnly
 * @see AutoApi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Hidden {
    // Marker annotation - no attributes needed
}
