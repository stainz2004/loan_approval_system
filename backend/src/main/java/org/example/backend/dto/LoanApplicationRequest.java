package org.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoanApplicationRequest {

    @NotNull
    @Size(max = 32)
    private String firstName;

    @NotNull
    @Size(max = 32)
    private String lastName;

    @NotNull
    private String idCode;

    @Max(360)
    @Min(6)
    private int loanLength;

    private double interest;

    private double baseInterest;

    @Min(5000)
    private long loanAmount;
}
