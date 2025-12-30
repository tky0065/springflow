package io.springflow.core.controller;

import io.springflow.core.exception.DuplicateEntityException;
import io.springflow.core.exception.EntityNotFoundException;
import io.springflow.core.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for SpringFlow REST API.
 * <p>
 * Provides centralized exception handling across all controllers,
 * converting exceptions into standardized error responses with appropriate HTTP status codes.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final String basePath;
    private final boolean logBotRequests;
    private final List<String> botPatterns;

    /**
     * Default constructor with standard bot patterns.
     */
    public GlobalExceptionHandler() {
        this("/api", false, Arrays.asList(
                ".php", "wp-admin", "wp-content", "wp-includes",
                ".asp", ".aspx", "phpmyadmin", "admin/",
                "cgi-bin", ".env", ".git"
        ));
    }

    /**
     * Constructor with custom configuration.
     *
     * @param basePath        the API base path (e.g., "/api")
     * @param logBotRequests  whether to log bot requests at INFO level (false = DEBUG level)
     * @param botPatterns     list of path patterns that identify bot/scanner requests
     */
    public GlobalExceptionHandler(String basePath, boolean logBotRequests, List<String> botPatterns) {
        this.basePath = basePath != null ? basePath : "/api";
        this.logBotRequests = logBotRequests;
        this.botPatterns = botPatterns != null ? botPatterns : new ArrayList<>();
    }

    /**
     * Handle EntityNotFoundException - return HTTP 404 NOT FOUND.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        log.warn("Entity not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle DuplicateEntityException - return HTTP 409 CONFLICT.
     */
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntity(
            DuplicateEntityException ex,
            HttpServletRequest request) {
        log.warn("Duplicate entity: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle ValidationException - return HTTP 400 BAD REQUEST with field errors.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        error.setFieldErrors(ex.getFieldErrors());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle MethodArgumentNotValidException - return HTTP 400 BAD REQUEST with detailed field errors.
     * This is thrown by Spring's @Valid annotation when request body validation fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        int errorCount = ex.getBindingResult().getFieldErrorCount();
        log.warn("Validation failed: {} field error(s)", errorCount);

        // Build detailed validation errors
        List<ValidationFieldError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ValidationFieldError validationError = new ValidationFieldError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    fieldError.getRejectedValue(),
                    fieldError.getCode()
            );
            validationErrors.add(validationError);
            log.debug("Validation error: field='{}', message='{}', rejectedValue='{}'",
                    fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue());
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                String.format("Validation failed with %d error(s)", errorCount),
                request.getRequestURI()
        );
        error.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle IllegalArgumentException - return HTTP 400 BAD REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle NoResourceFoundException - return HTTP 404 NOT FOUND.
     * This exception is thrown by Spring when a static resource or endpoint is not found.
     * We use intelligent logging to reduce noise from bot/scanner requests.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        String path = request.getRequestURI();

        // Determine appropriate log level based on request type
        if (isBotRequest(path)) {
            // Bot/scanner requests - log at DEBUG or INFO level to reduce noise
            if (logBotRequests) {
                log.info("Bot request to non-existent resource: {}", path);
            } else {
                log.debug("Bot request to non-existent resource: {}", path);
            }
        } else if (isLikelyApiRequest(path)) {
            // Potential API endpoint not found - log at WARN level for visibility
            log.warn("API endpoint not found: {} - {}", path, ex.getMessage());
        } else {
            // Other static resources - log at DEBUG level
            log.debug("Static resource not found: {}", path);
        }

        // Return standard 404 response without exposing internal details
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "The requested resource was not found",
                path
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Check if the request path matches known bot/scanner patterns.
     *
     * @param path the request path
     * @return true if the path matches bot patterns
     */
    private boolean isBotRequest(String path) {
        if (botPatterns == null || botPatterns.isEmpty()) {
            return false;
        }

        for (String pattern : botPatterns) {
            if (path.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request path is likely an API endpoint request.
     *
     * @param path the request path
     * @return true if the path starts with the configured API base path
     */
    private boolean isLikelyApiRequest(String path) {
        return path.startsWith(basePath);
    }

    /**
     * Handle all other exceptions - return HTTP 500 INTERNAL SERVER ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
