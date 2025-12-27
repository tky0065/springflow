package io.springflow.core.validation;

/**
 * Marker interfaces for JSR-380 validation groups.
 * <p>
 * These groups allow different validation rules for different operations:
 * <ul>
 *   <li>{@link Create} - Validation rules for entity creation (POST)</li>
 *   <li>{@link Update} - Validation rules for entity updates (PUT/PATCH)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Example usage:</strong>
 * </p>
 * <pre>{@code
 * @Entity
 * public class Product {
 *     @Id
 *     private Long id;
 *
 *     @NotBlank(groups = {Create.class, Update.class})
 *     private String name;
 *
 *     @NotNull(groups = Create.class)  // Required only on creation
 *     @Min(value = 0, groups = {Create.class, Update.class})
 *     private Double price;
 *
 *     @Email(groups = Update.class)  // Only validated on update
 *     private String supplierEmail;
 * }
 * }</pre>
 * <p>
 * SpringFlow automatically applies the appropriate validation group based on the HTTP method:
 * </p>
 * <ul>
 *   <li>POST → {@link Create} group</li>
 *   <li>PUT → {@link Update} group</li>
 *   <li>PATCH → {@link Update} group</li>
 * </ul>
 *
 * @see jakarta.validation.groups.Default
 * @since 0.4.0
 */
public class ValidationGroups {

    /**
     * Validation group for entity creation operations (POST).
     * <p>
     * Use this group to mark validations that should only apply when creating a new entity.
     * </p>
     * <p>
     * <strong>Example:</strong> Requiring a field only on creation but allowing it to be optional on updates.
     * </p>
     * <pre>{@code
     * @NotNull(groups = Create.class)
     * private String initialCategory;
     * }</pre>
     */
    public interface Create {
    }

    /**
     * Validation group for entity update operations (PUT/PATCH).
     * <p>
     * Use this group to mark validations that should only apply when updating an existing entity.
     * </p>
     * <p>
     * <strong>Example:</strong> Validating a field only when it's being updated.
     * </p>
     * <pre>{@code
     * @Email(groups = Update.class)
     * private String newEmail;
     * }</pre>
     */
    public interface Update {
    }

    private ValidationGroups() {
        // Utility class, prevent instantiation
    }
}
