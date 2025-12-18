package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as read-only in the REST API.
 *
 * <p>Fields annotated with {@code @ReadOnly} will:
 * <ul>
 *   <li>Be excluded from Input DTOs (cannot be set via POST/PUT)</li>
 *   <li>Be included in Output DTOs (visible in GET responses)</li>
 *   <li>Be documented as read-only in OpenAPI schema</li>
 * </ul>
 *
 * <p>Useful for computed fields, auto-generated values, or fields
 * that should only be modified internally.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "orders")
 * public class Order {
 *     @Id
 *     @ReadOnly
 *     private Long id;
 *
 *     private String customerName;
 *
 *     @ReadOnly
 *     private BigDecimal totalAmount; // Computed from items
 *
 *     @ReadOnly
 *     private LocalDateTime createdAt;
 *
 *     @OneToMany
 *     private List<OrderItem> items;
 * }
 * }</pre>
 *
 * <p>Clients can view {@code id}, {@code totalAmount}, and {@code createdAt}
 * but cannot modify them via the API.
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see Hidden
 * @see AutoApi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {
    // Marker annotation - no attributes needed
}
