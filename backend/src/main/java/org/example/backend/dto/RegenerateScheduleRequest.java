package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Schema(name = "RegenerateScheduleRequest", description = "Payload for regenerating a loan payment schedule based on updated loan parameters")
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