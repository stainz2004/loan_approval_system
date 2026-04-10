package org.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.dto.LoanRejectionReason;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String personalCode;
    private Integer loanPeriodMonths;
    private BigDecimal interestMargin;
    private BigDecimal baseInterest;
    private BigDecimal loanAmount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LoanApplicationStatus loanApplicationStatus;
    @Enumerated(EnumType.STRING)
    private LoanRejectionReason rejectionReason;
}
