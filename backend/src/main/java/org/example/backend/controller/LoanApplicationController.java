package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanRejectionReason;
import org.example.backend.dto.ValidationDecision;
import org.example.backend.service.LoanApplicationQueryService;
import org.example.backend.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final LoanApplicationQueryService loanApplicationQueryService;

    @PostMapping()
    public ResponseEntity<ValidationDecision> requestLoanDecision(@Valid @RequestBody LoanApplicationRequest loanApplicationRequest) {
        return ResponseEntity.status(201).body(loanApplicationService.createLoanApplication(loanApplicationRequest));
    }

    @GetMapping()
    public ResponseEntity<List<LoanApplicationResponse>> getLoanApplications() {
        return ResponseEntity.ok(loanApplicationQueryService.getLoanApplications());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveLoanApplication(@PathVariable Long id) {
        loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectLoanApplication(@PathVariable Long id, @RequestParam LoanRejectionReason reason) {
        loanApplicationService.rejectLoanApplication(id, reason);
        return ResponseEntity.ok().build();
    }
}
