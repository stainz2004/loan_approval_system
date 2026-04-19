package org.example.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the status of a loan application.
 */
@Schema(description = "Status of a loan application")
public enum LoanApplicationStatus {
    STARTED,
    IN_REVIEW,
    APPROVED,
    REJECTED
}
