package io.springflow.core.dto;

import java.util.List;

public record SearchRequest(
    List<FilterCriteria> criteria,
    LogicalOperator operator // AND/OR
) {
    public enum LogicalOperator {
        AND, OR
    }

    public record FilterCriteria(
        String field,
        FilterOperator operator,
        Object value
    ) {}
}
