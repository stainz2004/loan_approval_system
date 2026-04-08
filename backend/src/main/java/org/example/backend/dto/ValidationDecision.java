package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ValidationDecision {
    private boolean accepted;
    private String rejectionReason;
}
