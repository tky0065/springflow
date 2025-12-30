package io.springflow.core.controller;

import io.springflow.core.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(
                "/api",
                false,
                Arrays.asList(".php", "wp-admin", ".asp", ".env")
        );
        request = new MockHttpServletRequest();
    }

    @Test
    void handleNoResourceFound_withBotRequest_shouldReturnNotFound() {
        // Given
        request.setRequestURI("/index.php/apps/files/preview-service-worker.js");
        NoResourceFoundException ex = new NoResourceFoundException(
                HttpMethod.GET,
                "/index.php/apps/files/preview-service-worker.js",
                "Resource not found"
        );

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("The requested resource was not found");
        assertThat(response.getBody().getPath()).isEqualTo("/index.php/apps/files/preview-service-worker.js");
    }

    @Test
    void handleNoResourceFound_withWpAdminRequest_shouldReturnNotFound() {
        // Given
        request.setRequestURI("/wp-admin/admin.php");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/wp-admin/admin.php", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleNoResourceFound_withApiRequest_shouldReturnNotFound() {
        // Given
        request.setRequestURI("/api/nonexistent");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/nonexistent", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getPath()).isEqualTo("/api/nonexistent");
    }

    @Test
    void handleNoResourceFound_withStaticResource_shouldReturnNotFound() {
        // Given
        request.setRequestURI("/static/image.png");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/static/image.png", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleNoResourceFound_withDefaultConstructor_shouldReturnNotFound() {
        // Given
        GlobalExceptionHandler handlerWithDefaults = new GlobalExceptionHandler();
        request.setRequestURI("/index.php/test");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/index.php/test", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = handlerWithDefaults.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void handleNoResourceFound_withEmptyBotPatterns_shouldReturnNotFound() {
        // Given
        GlobalExceptionHandler handlerWithEmptyPatterns = new GlobalExceptionHandler(
                "/api",
                false,
                Collections.emptyList()
        );
        request.setRequestURI("/index.php/test");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/index.php/test", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = handlerWithEmptyPatterns.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void handleNoResourceFound_withCustomBasePath_shouldDetectApiRequest() {
        // Given
        GlobalExceptionHandler customHandler = new GlobalExceptionHandler(
                "/custom-api",
                false,
                Arrays.asList(".php", "wp-admin")
        );
        request.setRequestURI("/custom-api/products");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/custom-api/products", "Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = customHandler.handleNoResourceFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/custom-api/products");
    }

    @Test
    void handleEntityNotFound_shouldReturnNotFound() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException(String.class, 123L);
        request.setRequestURI("/api/entities/123");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("String");
        assertThat(response.getBody().getMessage()).contains("123");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");
        request.setRequestURI("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequest() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        request.setRequestURI("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
    }
}
