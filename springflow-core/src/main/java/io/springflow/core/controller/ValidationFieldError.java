package io.springflow.core.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Detailed field error information for validation failures.
 * <p>
 * This class provides comprehensive information about a validation error on a specific field,
 * including the field name, error message, rejected value, and the validation constraint that failed.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationFieldError {

    private String field;
    private String message;
    private Object rejectedValue;
    private String code;

    public ValidationFieldError() {
    }

    public ValidationFieldError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ValidationFieldError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public ValidationFieldError(String field, String message, Object rejectedValue, String code) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
        this.code = code;
    }

    // Getters and Setters

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ValidationFieldError{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", code='" + code + '\'' +
                '}';
    }
}
