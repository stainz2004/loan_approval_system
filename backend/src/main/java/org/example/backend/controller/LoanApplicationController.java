package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Loan Applications", description = "Endpoints for creating, reviewing, and deciding loan applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final LoanApplicationQueryService loanApplicationQueryService;

    @Operation(summary = "Create loan application and get initial decision")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan application created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping
    public ResponseEntity<ValidationDecision> requestLoanDecision(
            @Valid @RequestBody LoanApplicationRequest loanApplicationRequest) {
        return ResponseEntity.status(201).body(loanApplicationService.createLoanApplication(loanApplicationRequest));
    }

    @Operation(summary = "Get loan applications in review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan applications retrieved")
    })
    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> getLoanApplications() {
        return ResponseEntity.ok(loanApplicationQueryService.getLoanApplications());
    }

    @Operation(summary = "Approve a loan application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan application approved"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Invalid application state")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveLoanApplication(@PathVariable Long id) {
        loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject a loan application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan application rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid rejection reason"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Invalid application state")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectLoanApplication(
            @PathVariable Long id,
            @RequestParam LoanRejectionReason reason) {
        loanApplicationService.rejectLoanApplication(id, reason);
        return ResponseEntity.ok().build();
    }
}