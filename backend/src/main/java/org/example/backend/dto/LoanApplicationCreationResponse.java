package org.example.backend.dto;

import java.util.List;

public record LoanApplicationCreationResponse(
        boolean isAccepted,
        LoanRejectionReason rejectionReason,
        List<PaymentScheduleItemDTO> paymentScheduleItems
) {
    public static LoanApplicationCreationResponse accepted(List<PaymentScheduleItemDTO> items) {
        return new LoanApplicationCreationResponse(true, null, items);
    }

    public static LoanApplicationCreationResponse rejected(LoanRejectionReason reason) {
        return new LoanApplicationCreationResponse(false, reason, null);
    }
}