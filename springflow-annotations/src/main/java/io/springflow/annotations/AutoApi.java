package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity to automatically generate a complete REST API with CRUD operations.
 *
 * <p>When applied to an entity class, SpringFlow will automatically generate:
 * <ul>
 *   <li>JpaRepository with JpaSpecificationExecutor for dynamic queries</li>
 *   <li>Service layer with transaction management</li>
 *   <li>REST controller with full CRUD endpoints</li>
 *   <li>Input and Output DTOs with validation</li>
 *   <li>OpenAPI/Swagger documentation</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(
 *     path = "products",
 *     expose = Expose.ALL,
 *     pagination = true,
 *     sorting = true
 * )
 * public class Product {
 *     @Id
 *     @GeneratedValue
 *     private Long id;
 *
 *     @NotBlank
 *     private String name;
 *
 *     private BigDecimal price;
 * }
 * }</pre>
 *
 * <p>This will generate endpoints:
 * <ul>
 *   <li>GET /api/products - List all with pagination</li>
 *   <li>GET /api/products/{id} - Get by ID</li>
 *   <li>POST /api/products - Create new</li>
 *   <li>PUT /api/products/{id} - Update existing</li>
 *   <li>PATCH /api/products/{id} - Partial update</li>
 *   <li>DELETE /api/products/{id} - Delete</li>
 * </ul>
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see Expose
 * @see Security
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoApi {

    /**
     * The base path for the REST endpoints (without leading slash).
     *
     * <p>If not specified, defaults to the lowercase, pluralized entity name.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code path = "products"} → /api/products</li>
     *   <li>{@code path = "admin/users"} → /api/admin/users</li>
     * </ul>
     *
     * @return the REST API path
     */
    String path() default "";

    /**
     * Controls which CRUD operations are exposed via the REST API.
     *
     * <p>Defaults to {@link Expose#ALL} which generates all CRUD endpoints.
     *
     * @return the exposure level
     * @see Expose
     */
    Expose expose() default Expose.ALL;

    /**
     * Security configuration for the API endpoints.
     *
     * <p>Defaults to public access. Can be configured to require authentication
     * or specific roles.
     *
     * @return the security configuration
     * @see Security
     */
    Security security() default @Security;

    /**
     * Enable pagination support for list endpoints.
     *
     * <p>When enabled, GET endpoints will support:
     * <ul>
     *   <li>{@code page} - Page number (0-indexed)</li>
     *   <li>{@code size} - Page size</li>
     * </ul>
     *
     * <p>Defaults to {@code true}.
     *
     * @return true if pagination is enabled
     */
    boolean pagination() default true;

    /**
     * Enable sorting support for list endpoints.
     *
     * <p>When enabled, GET endpoints will support:
     * <ul>
     *   <li>{@code sort=field,asc} - Sort ascending</li>
     *   <li>{@code sort=field,desc} - Sort descending</li>
     *   <li>Multiple sort parameters for multi-field sorting</li>
     * </ul>
     *
     * <p>Defaults to {@code true}.
     *
     * @return true if sorting is enabled
     */
    boolean sorting() default true;

    /**
     * Description of the API for OpenAPI documentation.
     *
     * <p>Used to generate meaningful API documentation in Swagger UI.
     * If not specified, a default description is generated.
     *
     * @return the API description
     */
    String description() default "";

    /**
     * Tags for grouping in OpenAPI documentation.
     *
     * <p>Used to organize endpoints in Swagger UI.
     * If not specified, the entity name is used as the tag.
     *
     * @return the OpenAPI tags
     */
    String[] tags() default {};
}
