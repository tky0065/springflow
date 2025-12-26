package io.springflow.graphql.filter;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Represents a GraphQL filter argument.
 * <p>
 * This class is used to parse GraphQL filter inputs and convert them
 * to a format compatible with SpringFlow's FilterResolver.
 * </p>
 *
 * <p>Supported filter operations:</p>
 * <ul>
 *   <li>eq - Equals</li>
 *   <li>like - Contains (case-sensitive or insensitive based on @Filterable config)</li>
 *   <li>gt - Greater than</li>
 *   <li>gte - Greater than or equal</li>
 *   <li>lt - Less than</li>
 *   <li>lte - Less than or equal</li>
 *   <li>in - In list</li>
 *   <li>notIn - Not in list</li>
 *   <li>isNull - Is null / is not null</li>
 *   <li>between - Between two values</li>
 * </ul>
 *
 * <p>Example GraphQL query with filters:</p>
 * <pre>
 * query {
 *   products(
 *     page: 0,
 *     size: 10,
 *     filters: {
 *       name: { like: "Laptop" }
 *       price: { gte: 500, lte: 2000 }
 *       category: { eq: "Electronics" }
 *     }
 *   ) {
 *     content { id, name, price }
 *     pageInfo { totalElements }
 *   }
 * }
 * </pre>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Data
public class GraphQLFilter {

    /**
     * Map of field names to their filter criteria.
     * <p>
     * Key: field name (e.g., "name", "price")
     * Value: filter criteria for that field
     * </p>
     */
    private Map<String, FieldFilter> fields;

    /**
     * Represents filter criteria for a single field.
     */
    @Data
    public static class FieldFilter {
        /** Equals filter */
        private String eq;

        /** Like/contains filter */
        private String like;

        /** Greater than filter */
        private String gt;

        /** Greater than or equal filter */
        private String gte;

        /** Less than filter */
        private String lt;

        /** Less than or equal filter */
        private String lte;

        /** In list filter */
        private List<String> in;

        /** Not in list filter */
        private List<String> notIn;

        /** Is null filter (true = is null, false = is not null) */
        private Boolean isNull;

        /** Between filter (array of exactly 2 values: [min, max]) */
        private List<String> between;
    }
}
