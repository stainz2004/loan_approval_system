package org.example.backend.exception;

/**
 * Exception thrown when an invalid personal code is provided.
 */
public class InvalidPersonalCodeException extends RuntimeException {
    public InvalidPersonalCodeException(String message) {
        super(message);
    }
}
