package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Response object for loan applications with IN-REVIEW status, containing application details and payment schedule")
public record LoanApplicationResponse(
        Long id,
        String firstName,
        String lastName,
        String personalCode,
        Integer loanPeriodMonths,
        BigDecimal interestMargin,
        BigDecimal baseInterest,
        BigDecimal loanAmount,
        List<PaymentScheduleItemDTO> paymentScheduleItems
) {}
