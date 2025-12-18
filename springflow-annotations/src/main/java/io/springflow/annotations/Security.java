package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security configuration for auto-generated API endpoints.
 *
 * <p>Used within {@link AutoApi} to configure authentication and authorization.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(
 *     path = "admin/users",
 *     security = @Security(
 *         level = SecurityLevel.ROLE_BASED,
 *         roles = {"ADMIN", "MANAGER"}
 *     )
 * )
 * public class User {
 *     // ...
 * }
 * }</pre>
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see AutoApi
 * @see SecurityLevel
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Security {

    /**
     * The security level to apply to the endpoints.
     *
     * <p>Defaults to {@link SecurityLevel#PUBLIC}.
     *
     * @return the security level
     * @see SecurityLevel
     */
    SecurityLevel level() default SecurityLevel.PUBLIC;

    /**
     * Required roles for access when using {@link SecurityLevel#ROLE_BASED}.
     *
     * <p>Users must have at least one of the specified roles.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code roles = {"ADMIN"}} - Only admins</li>
     *   <li>{@code roles = {"ADMIN", "MANAGER"}} - Admins or managers</li>
     * </ul>
     *
     * @return the required roles
     */
    String[] roles() default {};

    /**
     * Required authorities for access when using {@link SecurityLevel#ROLE_BASED}.
     *
     * <p>More fine-grained than roles. Users must have at least one authority.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code authorities = {"user:read", "user:write"}}</li>
     * </ul>
     *
     * @return the required authorities
     */
    String[] authorities() default {};
}
