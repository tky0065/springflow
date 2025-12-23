package io.springflow.annotations;

/**
 * Defines the security level for API endpoints.
 *
 * <p>Used with {@link Security} annotation to configure authentication and authorization.
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see Security
 * @see AutoApi
 */
public enum SecurityLevel {

    /**
     * Public access - no authentication required.
     *
     * <p>All endpoints are accessible without authentication.
     * Useful for public APIs and read-only resources.
     */
    PUBLIC,

    /**
     * Requires authentication but no specific roles.
     *
     * <p>Any authenticated user can access the endpoints.
     * The user must have a valid JWT token or active session.
     */
    AUTHENTICATED,

    /**
     * Requires specific roles or authorities.
     *
     * <p>User must be authenticated AND have at least one of the
     * specified roles or authorities in the {@link Security} annotation.
     *
     * <p>Example:
     * <pre>{@code
     * @Security(
     *     level = SecurityLevel.ROLE_BASED,
     *     roles = {"ADMIN", "MANAGER"}
     * )
     * }</pre>
     */
    ROLE_BASED,

    /**
     * Inherit security level from a higher level configuration or use default.
     */
    UNDEFINED
}
