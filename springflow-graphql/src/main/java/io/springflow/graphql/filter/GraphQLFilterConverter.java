package io.springflow.graphql.filter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts GraphQL filter arguments to query parameter format compatible with FilterResolver.
 * <p>
 * This converter bridges the gap between GraphQL's structured filter format and
 * SpringFlow's REST-style query parameter format.
 * </p>
 *
 * <p>Conversion examples:</p>
 * <pre>
 * GraphQL: { name: { like: "Laptop" } }
 * → Map: { "name_like": "Laptop" }
 *
 * GraphQL: { price: { gte: "500", lte: "2000" } }
 * → Map: { "price_gte": "500", "price_lte": "2000" }
 *
 * GraphQL: { category: { in: ["Electronics", "Computers"] } }
 * → Map: { "category_in": "Electronics,Computers" }
 * </pre>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Component
public class GraphQLFilterConverter {

    /**
     * Converts GraphQL filter to query parameter map.
     *
     * @param filter the GraphQL filter
     * @return a map of query parameters compatible with FilterResolver
     */
    public Map<String, String> convertToParams(GraphQLFilter filter) {
        if (filter == null || filter.getFields() == null) {
            return new HashMap<>();
        }

        Map<String, String> params = new HashMap<>();

        filter.getFields().forEach((fieldName, fieldFilter) -> {
            if (fieldFilter == null) {
                return;
            }

            // EQUALS: field=value
            if (fieldFilter.getEq() != null) {
                params.put(fieldName, fieldFilter.getEq());
            }

            // LIKE: field_like=value
            if (fieldFilter.getLike() != null) {
                params.put(fieldName + "_like", fieldFilter.getLike());
            }

            // GREATER_THAN: field_gt=value
            if (fieldFilter.getGt() != null) {
                params.put(fieldName + "_gt", fieldFilter.getGt());
            }

            // GREATER_THAN_OR_EQUAL: field_gte=value
            if (fieldFilter.getGte() != null) {
                params.put(fieldName + "_gte", fieldFilter.getGte());
            }

            // LESS_THAN: field_lt=value
            if (fieldFilter.getLt() != null) {
                params.put(fieldName + "_lt", fieldFilter.getLt());
            }

            // LESS_THAN_OR_EQUAL: field_lte=value
            if (fieldFilter.getLte() != null) {
                params.put(fieldName + "_lte", fieldFilter.getLte());
            }

            // IN: field_in=v1,v2,v3
            if (fieldFilter.getIn() != null && !fieldFilter.getIn().isEmpty()) {
                String commaSeparated = String.join(",", fieldFilter.getIn());
                params.put(fieldName + "_in", commaSeparated);
            }

            // NOT_IN: field_not_in=v1,v2,v3
            if (fieldFilter.getNotIn() != null && !fieldFilter.getNotIn().isEmpty()) {
                String commaSeparated = String.join(",", fieldFilter.getNotIn());
                params.put(fieldName + "_not_in", commaSeparated);
            }

            // IS_NULL: field_null=true/false
            if (fieldFilter.getIsNull() != null) {
                params.put(fieldName + "_null", fieldFilter.getIsNull().toString());
            }

            // BETWEEN: field_between=min,max
            if (fieldFilter.getBetween() != null && fieldFilter.getBetween().size() == 2) {
                String betweenValue = fieldFilter.getBetween().get(0) + "," + fieldFilter.getBetween().get(1);
                params.put(fieldName + "_between", betweenValue);
            }
        });

        return params;
    }

    /**
     * Converts a simple map-based filter to query parameter map.
     * <p>
     * This is a convenience method for simple filters where each field
     * has a single string value (equals operation).
     * </p>
     *
     * @param filters simple map of field names to values
     * @return a map of query parameters
     */
    public Map<String, String> convertSimpleFilter(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new HashMap<>();
        }

        return filters.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
    }
}
