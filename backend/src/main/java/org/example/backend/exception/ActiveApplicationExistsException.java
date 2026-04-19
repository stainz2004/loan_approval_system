package org.example.backend.exception;

/**
 * Exception thrown when a customer tries to create a new loan application while they already have an active one.
 */
public class ActiveApplicationExistsException extends RuntimeException {
    public ActiveApplicationExistsException() {
        super("Customer already has an active IN_REVIEW loan application.");
    }
}