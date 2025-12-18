package io.springflow.annotations;

/**
 * Defines which CRUD operations are exposed via the REST API.
 *
 * <p>Used with {@link AutoApi} to control endpoint generation.
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see AutoApi
 */
public enum Expose {

    /**
     * Expose all CRUD operations.
     *
     * <p>Generates endpoints:
     * <ul>
     *   <li>GET /resource - List all</li>
     *   <li>GET /resource/{id} - Get by ID</li>
     *   <li>POST /resource - Create</li>
     *   <li>PUT /resource/{id} - Full update</li>
     *   <li>PATCH /resource/{id} - Partial update</li>
     *   <li>DELETE /resource/{id} - Delete</li>
     * </ul>
     */
    ALL,

    /**
     * Expose only read operations (GET).
     *
     * <p>Generates endpoints:
     * <ul>
     *   <li>GET /resource - List all</li>
     *   <li>GET /resource/{id} - Get by ID</li>
     * </ul>
     *
     * <p>Useful for read-only resources or reporting APIs.
     */
    READ_ONLY,

    /**
     * Expose create and update operations, but not delete.
     *
     * <p>Generates endpoints:
     * <ul>
     *   <li>GET /resource - List all</li>
     *   <li>GET /resource/{id} - Get by ID</li>
     *   <li>POST /resource - Create</li>
     *   <li>PUT /resource/{id} - Full update</li>
     *   <li>PATCH /resource/{id} - Partial update</li>
     * </ul>
     *
     * <p>Useful for resources that should never be physically deleted,
     * often combined with {@link SoftDelete}.
     */
    CREATE_UPDATE,

    /**
     * Custom exposure - allows fine-grained control.
     *
     * <p>When using CUSTOM, specific endpoints can be enabled/disabled
     * via additional configuration (Phase 2 feature).
     *
     * <p>Reserved for future use.
     */
    CUSTOM
}
