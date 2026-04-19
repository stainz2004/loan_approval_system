package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.backend.entity.LoanRejectionReason;

@Schema(name = "LoanApplicationRejectRequest",
        description = "Payload for rejecting a loan application")
public record LoanApplicationRejectRequest(
    @NotNull(message = "Rejection reason must not be null")
    @Schema(description = "Reason for rejection")
    LoanRejectionReason reason
) {}