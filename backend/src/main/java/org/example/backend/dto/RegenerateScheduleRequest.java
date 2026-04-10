package org.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record RegenerateScheduleRequest(
        @DecimalMin("5000")
        BigDecimal loanAmount,

        @DecimalMin("0")
        BigDecimal interestMargin,

        @DecimalMin("0")
        BigDecimal baseInterest,

        @Min(6) @Max(360)
        Integer loanPeriodMonths
) {}