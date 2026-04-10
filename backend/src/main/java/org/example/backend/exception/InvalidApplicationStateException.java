package org.example.backend.exception;

/**
 * Exception thrown when an operation is attempted on an application that is in an invalid state.
 */
public class InvalidApplicationStateException extends RuntimeException {
    public InvalidApplicationStateException(String message) {
        super(message);
    }
}