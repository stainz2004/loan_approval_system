package org.example.backend.exception;

/**
 * Exception thrown when a loan application with a specified ID is not found.
 */
public class LoanApplicationNotFoundException extends RuntimeException {
    public LoanApplicationNotFoundException(Long id) {
        super("Loan application not found with id: " + id);
    }
}