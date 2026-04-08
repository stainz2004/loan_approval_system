package org.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotBlank
    @Size(max = 32)
    private String firstName;

    @NotBlank
    @Size(max = 32)
    private String lastName;

    @NotBlank
    @Size(min = 11, max = 11)
    private String personalCode;

    @NotNull
    @Max(360)
    @Min(6)
    private Integer loanPeriodMonths;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal interestMargin;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal baseInterest;

    @NotNull
    @DecimalMin("5000.0")
    private BigDecimal loanAmount;
}
