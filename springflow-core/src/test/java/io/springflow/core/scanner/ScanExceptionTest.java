package io.springflow.core.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ScanException}.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
@DisplayName("ScanException Tests")
class ScanExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void testCreateWithMessage() {
        // Given
        String message = "Scan failed";

        // When
        ScanException exception = new ScanException(message);

        // Then
        assertThat(exception)
            .hasMessage(message)
            .hasNoCause();
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testCreateWithMessageAndCause() {
        // Given
        String message = "Scan failed";
        Throwable cause = new IllegalStateException("Invalid state");

        // When
        ScanException exception = new ScanException(message, cause);

        // Then
        assertThat(exception)
            .hasMessage(message)
            .hasCause(cause);
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void testIsRuntimeException() {
        // Given
        ScanException exception = new ScanException("Test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}

