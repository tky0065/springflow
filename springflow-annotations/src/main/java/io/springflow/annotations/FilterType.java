package io.springflow.annotations;

/**
 * Defines the types of filters that can be applied to entity fields.
 *
 * <p>Used with {@link Filterable} annotation to enable dynamic query filtering.
 *
 * @author SpringFlow
 * @since 0.1.0
 * @see Filterable
 */
public enum FilterType {

    /**
     * Exact match filter.
     *
     * <p>Query parameter: {@code ?field=value}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?name=John} - Matches name exactly "John"</li>
     *   <li>{@code ?status=ACTIVE} - Matches status ACTIVE</li>
     *   <li>{@code ?age=25} - Matches age exactly 25</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field = ?}
     */
    EQUALS,

    /**
     * Pattern matching filter for strings.
     *
     * <p>Query parameter: {@code ?field_like=pattern}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?name_like=John} - Matches names containing "John"</li>
     *   <li>{@code ?email_like=@gmail} - Matches Gmail addresses</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field LIKE '%?%'}
     */
    LIKE,

    /**
     * Greater than comparison.
     *
     * <p>Query parameter: {@code ?field_gt=value}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_gt=18} - Age greater than 18</li>
     *   <li>{@code ?price_gt=100} - Price greater than 100</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field > ?}
     */
    GREATER_THAN,

    /**
     * Less than comparison.
     *
     * <p>Query parameter: {@code ?field_lt=value}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_lt=65} - Age less than 65</li>
     *   <li>{@code ?price_lt=1000} - Price less than 1000</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field < ?}
     */
    LESS_THAN,

    /**
     * Greater than or equal comparison.
     *
     * <p>Query parameter: {@code ?field_gte=value}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_gte=18} - Age 18 or older</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field >= ?}
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * Less than or equal comparison.
     *
     * <p>Query parameter: {@code ?field_lte=value}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_lte=65} - Age 65 or younger</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field <= ?}
     */
    LESS_THAN_OR_EQUAL,

    /**
     * Range filter combining gte and lte.
     *
     * <p>Query parameters: {@code ?field_gte=min&field_lte=max}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_gte=18&age_lte=65} - Age between 18 and 65</li>
     *   <li>{@code ?price_gte=100&price_lte=500} - Price between 100 and 500</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field BETWEEN ? AND ?}
     */
    RANGE,

    /**
     * IN clause filter for multiple values.
     *
     * <p>Query parameter: {@code ?field_in=value1,value2,value3}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?status_in=ACTIVE,PENDING} - Status is ACTIVE or PENDING</li>
     *   <li>{@code ?id_in=1,2,3,4,5} - ID is one of 1,2,3,4,5</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field IN (?, ?, ?)}
     */
    IN,

    /**
     * NOT IN clause filter for excluding values.
     *
     * <p>Query parameter: {@code ?field_not_in=value1,value2}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?status_not_in=DELETED,ARCHIVED}</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field NOT IN (?, ?)}
     */
    NOT_IN,

    /**
     * NULL check filter.
     *
     * <p>Query parameter: {@code ?field_null=true} or {@code ?field_null=false}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?deletedAt_null=true} - Find non-deleted records</li>
     *   <li>{@code ?email_null=false} - Find records with email</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field IS NULL} or {@code WHERE field IS NOT NULL}
     */
    IS_NULL,

    /**
     * BETWEEN filter with explicit min and max.
     *
     * <p>Query parameter: {@code ?field_between=min,max}
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code ?age_between=18,65}</li>
     *   <li>{@code ?date_between=2024-01-01,2024-12-31}</li>
     * </ul>
     *
     * <p>SQL equivalent: {@code WHERE field BETWEEN ? AND ?}
     */
    BETWEEN
}
