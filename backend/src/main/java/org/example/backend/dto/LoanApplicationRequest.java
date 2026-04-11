package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "LoanApplicationRequest", description = "Payload for submitting a new loan application")
public record LoanApplicationRequest(
        @NotBlank @Size(max = 32) String firstName,
        @NotBlank @Size(max = 32) String lastName,
        @NotBlank @Size(min = 11, max = 11) String personalCode,
        @NotNull @Min(6) @Max(360) Integer loanPeriodMonths,
        @NotNull @DecimalMin("0.1") BigDecimal interestMargin,
        @NotNull @DecimalMin("0.1") BigDecimal baseInterest,
        @NotNull @DecimalMin("5000.0") BigDecimal loanAmount
) {}
