package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the result of a loan application validation.")
public record ValidationDecision(boolean isAccepted, LoanRejectionReason rejectionReason) {

    public static ValidationDecision accepted() {
        return new ValidationDecision(true, null);
    }

    public static ValidationDecision rejected(LoanRejectionReason reason) {
        return new ValidationDecision(false, reason);
    }
}