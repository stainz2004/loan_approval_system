package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationCreationResponse;
import org.example.backend.dto.LoanApplicationRejectRequest;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.RegenerateScheduleRequest;
import org.example.backend.service.LoanApplicationQueryService;
import org.example.backend.service.LoanApplicationService;
import org.springframework.http.HttpStatus;
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


    @Operation(summary = "Create a loan application")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Loan application created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping
    public ResponseEntity<LoanApplicationCreationResponse> requestLoanDecision(
            @Valid @RequestBody LoanApplicationRequest loanApplicationRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanApplicationService.createLoanApplication(loanApplicationRequest));
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
            @Valid @RequestBody LoanApplicationRejectRequest request) {
        loanApplicationService.rejectLoanApplication(id, request.reason());
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Regenerate the payment schedule based on new inputs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment schedule regenerated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Invalid application state")
    })
    @PostMapping("/{id}/regenerate-schedule")
    public ResponseEntity<Void> regenerateSchedule(@PathVariable Long id, @Valid @RequestBody RegenerateScheduleRequest request) {
        loanApplicationService.regenerateSchedule(id, request);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Get in-review payment schedule by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment schedule fetched"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/{id}/payment-schedule")
    public ResponseEntity<LoanApplicationResponse> getPaymentScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(loanApplicationQueryService.getPaymentScheduleById(id));
    }


    @Operation(summary = "Get all approved loan applications by personal code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan applications fetched"),
            @ApiResponse(responseCode = "400", description = "Invalid personal code"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/approved")
    public ResponseEntity<List<LoanApplicationResponse>> getApprovedLoanApplications(@RequestParam String personalCode) {
        return ResponseEntity.ok(loanApplicationQueryService.getApprovedLoanApplications(personalCode));
    }
}