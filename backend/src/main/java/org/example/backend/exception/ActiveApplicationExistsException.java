package org.example.backend.exception;

/**
 * Exception thrown when a customer tries to create a new loan application while they already have an active one.
 */
public class ActiveApplicationExistsException extends RuntimeException {
    public ActiveApplicationExistsException(String personalCode) {
        super("Customer already has an active loan application: " + personalCode);
    }
}