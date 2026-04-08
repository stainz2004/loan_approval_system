package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping()
    public ResponseEntity<Void> requestLoanDecision(@Valid @RequestBody LoanApplicationRequest loanApplicationRequest) {
        loanApplicationService.createLoanApplication(loanApplicationRequest);

        return ResponseEntity.status(201).build();
    }

    @GetMapping()
    public ResponseEntity<LoanApplicationResponse> getLoanApplications() {
        return null;
    }
}
