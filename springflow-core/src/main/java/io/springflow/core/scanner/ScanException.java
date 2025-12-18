package io.springflow.core.scanner;

/**
 * Exception thrown when entity scanning fails.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
public class ScanException extends RuntimeException {

    /**
     * Creates exception with message.
     *
     * @param message error message
     */
    public ScanException(String message) {
        super(message);
    }

    /**
     * Creates exception with message and cause.
     *
     * @param message error message
     * @param cause underlying cause
     */
    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
