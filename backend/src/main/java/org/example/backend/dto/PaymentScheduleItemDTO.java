package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentScheduleItemDTO {
    private Long id;
    private Integer paymentNumber;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal remainingBalance;
}
