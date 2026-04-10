package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Represents a single payment schedule item for a loan.")
public record PaymentScheduleItemDTO (
        Long id,
        Integer paymentNumber,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal remainingBalance
) {}
