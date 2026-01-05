package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be included in the summary DTO of the entity.
 *
 * <p>When this entity is referenced as a relationship in another entity,
 * only the fields marked with {@code @Summary} (plus the ID) will be 
 * included in the DTO by default.</p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi
 * public class User {
 *     @Id
 *     private Long id;
 *
 *     @Summary
 *     private String username;
 *
 *     private String email; // Included in full DTO, but not in summary
 *
 *     private String password; // Not included in any DTO (@Hidden/@ReadOnly)
 * }
 * }</pre>
 *
 * @author SpringFlow
 * @since 0.5.1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Summary {
}
