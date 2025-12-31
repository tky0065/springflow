package io.springflow.core.filter;

import io.springflow.annotations.FilterType;

/**
 * Pojo to hold filter criteria details.
 *
 * @author SpringFlow
 */
public class SearchCriteria {
    private String key;
    private FilterType operation;
    private Object value;

    public SearchCriteria() {
    }

    public SearchCriteria(String key, FilterType operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public FilterType getOperation() {
        return operation;
    }

    public void setOperation(FilterType operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}