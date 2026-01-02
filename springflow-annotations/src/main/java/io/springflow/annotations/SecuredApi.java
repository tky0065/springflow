package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifically configures security for an auto-generated API.
 *
 * <p>When applied to an entity class alongside {@link AutoApi}, this annotation
 * provides fine-grained control over authentication and authorization for the
 * generated CRUD endpoints.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "products")
 * @SecuredApi(
 *     level = SecurityLevel.ROLE_BASED,
 *     roles = {"ADMIN"},
 *     readLevel = SecurityLevel.PUBLIC
 * )
 * public class Product {
 *     // ...
 * }
 * }</pre>
 *
 * @author SpringFlow
 * @since 0.5.1
 * @see AutoApi
 * @see SecurityLevel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredApi {

    /**
     * The default security level for all endpoints of this API.
     *
     * <p>Defaults to {@link SecurityLevel#AUTHENTICATED}.
     *
     * @return the default security level
     */
    SecurityLevel level() default SecurityLevel.AUTHENTICATED;

    /**
     * Required roles for access when using {@link SecurityLevel#ROLE_BASED}.
     *
     * @return the required roles
     */
    String[] roles() default {};

    /**
     * Required authorities for access when using {@link SecurityLevel#ROLE_BASED}.
     *
     * @return the required authorities
     */
    String[] authorities() default {};

    /**
     * Security level for read operations (GET).
     *
     * <p>If set to {@link SecurityLevel#UNDEFINED}, the global {@link #level()} is used.
     *
     * @return the read security level
     */
    SecurityLevel readLevel() default SecurityLevel.UNDEFINED;

    /**
     * Security level for write operations (POST, PUT, PATCH, DELETE).
     *
     * <p>If set to {@link SecurityLevel#UNDEFINED}, the global {@link #level()} is used.
     *
     * @return the write security level
     */
    SecurityLevel writeLevel() default SecurityLevel.UNDEFINED;
}
