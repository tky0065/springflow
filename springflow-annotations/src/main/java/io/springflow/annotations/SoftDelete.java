package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables soft delete for a JPA entity.
 *
 * <p>When an entity is annotated with {@code @SoftDelete}, the generated
 * {@code deleteById} operation will not physically remove the record from the database.
 * Instead, it will mark it as deleted by setting a boolean flag or a timestamp.
 * </p>
 *
 * <p>Query operations will automatically filter out records marked as deleted,
 * unless configured otherwise.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "products")
 * @SoftDelete
 * public class Product {
 *     @Id
 *     private Long id;
 *
 *     private boolean deleted;
 *     private LocalDateTime deletedAt;
 * }
 * }</pre>
 *
 * @author SpringFlow
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoftDelete {

    /**
     * The name of the boolean field used to mark the record as deleted.
     *
     * <p>Defaults to "deleted".</p>
     *
     * @return the name of the deleted field
     */
    String deletedField() default "deleted";

    /**
     * The name of the timestamp field used to store the deletion date/time.
     *
     * <p>Defaults to "deletedAt". If the field is not present in the entity,
     * only the boolean flag will be used.</p>
     *
     * @return the name of the deletedAt field
     */
    String deletedAtField() default "deletedAt";
}