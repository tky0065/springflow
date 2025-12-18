package io.springflow.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity field as filterable in list endpoints.
 *
 * <p>When applied to a field, SpringFlow will automatically generate query parameters
 * that allow filtering based on the field value.
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @AutoApi(path = "products")
 * public class Product {
 *     @Id
 *     private Long id;
 *
 *     @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
 *     private String name;
 *
 *     @Filterable(types = FilterType.RANGE)
 *     private BigDecimal price;
 *
 *     @Filterable
 *     private ProductStatus status; // defaults to EQUALS
 * }
 * }</pre>
 *
 * <p>This generates query parameters:
 * <ul>
 *   <li>{@code ?name=laptop} - Exact match</li>
 *   <li>{@code ?name_like=lap} - Contains search</li>
 *   <li>{@code ?price_gte=100&price_lte=500} - Range filter</li>
 *   <li>{@code ?status=ACTIVE} - Enum filter</li>
 * </ul>
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see FilterType
 * @see AutoApi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Filterable {

    /**
     * The types of filters supported for this field.
     *
     * <p>Defaults to {@link FilterType#EQUALS}.
     *
     * <p>Multiple filter types can be specified to enable different
     * filtering strategies on the same field.
     *
     * <p>Examples:
     * <pre>{@code
     * @Filterable(types = FilterType.EQUALS)              // ?name=John
     * @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})  // ?name=John or ?name_like=Joh
     * @Filterable(types = FilterType.RANGE)               // ?age_gte=18&age_lte=65
     * }</pre>
     *
     * @return the supported filter types
     * @see FilterType
     */
    FilterType[] types() default FilterType.EQUALS;

    /**
     * Custom query parameter name for the filter.
     *
     * <p>If not specified, the field name is used.
     *
     * <p>Examples:
     * <ul>
     *   <li>Field: {@code firstName}, paramName: "" → {@code ?firstName=John}</li>
     *   <li>Field: {@code firstName}, paramName: "name" → {@code ?name=John}</li>
     * </ul>
     *
     * @return the custom parameter name, or empty string to use field name
     */
    String paramName() default "";

    /**
     * Description for OpenAPI documentation.
     *
     * <p>Used to document the filter parameter in Swagger UI.
     *
     * @return the filter description
     */
    String description() default "";

    /**
     * Whether the filter is case-sensitive.
     *
     * <p>Only applies to string filters (EQUALS, LIKE).
     * Defaults to {@code true}.
     *
     * @return true if case-sensitive
     */
    boolean caseSensitive() default true;
}
