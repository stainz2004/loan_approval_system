package org.example.backend.exception;

public class InvalidPersonalCodeException extends RuntimeException {
    public InvalidPersonalCodeException(String message) {
        super(message);
    }
}
