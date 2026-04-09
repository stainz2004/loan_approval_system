package org.example.backend.dto;

public record ValidationDecision(boolean isAccepted, LoanRejectionReason rejectionReason) {

    public static ValidationDecision accepted() {
        return new ValidationDecision(true, null);
    }

    public static ValidationDecision rejected(LoanRejectionReason reason) {
        return new ValidationDecision(false, reason);
    }
}