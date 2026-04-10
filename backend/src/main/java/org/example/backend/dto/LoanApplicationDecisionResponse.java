package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the result of a loan application validation.")
public record LoanApplicationDecisionResponse(boolean isAccepted, LoanRejectionReason rejectionReason) {

    public static LoanApplicationDecisionResponse accepted() {
        return new LoanApplicationDecisionResponse(true, null);
    }

    public static LoanApplicationDecisionResponse rejected(LoanRejectionReason reason) {
        return new LoanApplicationDecisionResponse(false, reason);
    }
}