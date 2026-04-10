package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the reasons for loan rejection.
 */
@Schema(description = "Enum representing the reasons for loan rejection.")
public enum LoanRejectionReason {
    CUSTOMER_TOO_OLD,
    INSUFFICIENT_DATA,
    OTHER
}