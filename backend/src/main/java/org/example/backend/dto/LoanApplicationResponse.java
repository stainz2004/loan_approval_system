package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LoanApplicationResponse {
    private String firstName;
    private String lastName;
    private String personalCode;
    private Integer loanPeriodMonths;
    private BigDecimal interestMargin;
    private BigDecimal baseInterest;
    private BigDecimal loanAmount;
    private List<PaymentScheduleItemDTO> paymentScheduleItems;
}
