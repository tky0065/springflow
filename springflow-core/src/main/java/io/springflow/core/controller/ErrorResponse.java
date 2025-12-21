package io.springflow.core.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response format for REST API errors.
 * <p>
 * This class provides a consistent error response structure across all SpringFlow endpoints,
 * including detailed validation errors when applicable.
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Simple field errors as a map (field name → error message).
     * @deprecated Use {@link #validationErrors} for detailed validation errors.
     */
    @Deprecated
    private Map<String, String> fieldErrors;

    /**
     * Detailed validation errors with field, message, rejected value, and code.
     */
    private List<ValidationFieldError> validationErrors;

    private List<String> errors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters and Setters

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<ValidationFieldError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationFieldError> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
