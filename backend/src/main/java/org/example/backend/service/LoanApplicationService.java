package org.example.backend.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.dto.LoanRejectionReason;
import org.example.backend.dto.RegenerateScheduleRequest;
import org.example.backend.dto.LoanApplicationDecisionResponse;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.exception.ActiveApplicationExistsException;
import org.example.backend.exception.InvalidApplicationStateException;
import org.example.backend.exception.LoanApplicationNotFoundException;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.example.backend.repository.PaymentScheduleRepository;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationValidator loanApplicationValidator;
    private final PaymentScheduleGenerator paymentScheduleGenerator;
    private final LoanApplicationMapper loanApplicationMapper;
    private final PaymentScheduleRepository paymentScheduleRepository;

    /**
     * Creates a new loan application based on the provided request. This method validates the request,
     *
     * @param request The loan application request containing the necessary information to create a loan application.
     */
    @Transactional
    public LoanApplicationDecisionResponse createLoanApplication(LoanApplicationRequest request) {
        if (loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                request.personalCode(),
                LoanApplicationStatus.IN_REVIEW)) {
            throw new ActiveApplicationExistsException("Customer already has an active IN_REVIEW application.");
        }

        loanApplicationValidator.validateCustomerPersonalCode(request.personalCode());

        LoanApplicationDecisionResponse decision = loanApplicationValidator.validateAge(request.personalCode());

        LoanApplication application = loanApplicationMapper.toEntity(request);

        if (!decision.isAccepted()) {
            application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
            application.setRejectionReason(decision.rejectionReason());
            loanApplicationRepository.save(application);
            return decision;
        }

        application.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        loanApplicationRepository.save(application);

        PaymentSchedule schedule = paymentScheduleGenerator.generateSchedule(application);
        paymentScheduleRepository.save(schedule);

        return decision;
    }

    /**
     * Regenerates the payment schedule for a loan application with the given ID based on the provided request.
     * This method validates that the application is in the IN_REVIEW status before allowing schedule regeneration.
     *
     * @param id The ID of the loan application for which to regenerate the payment schedule.
     * @param request The request containing the necessary information to regenerate the payment schedule, such as new loan amount or period.
     */
    @Transactional
    public void regenerateSchedule(Long id, RegenerateScheduleRequest request) {
        LoanApplication application = getApplicationOrThrow(id);

        if (application.getLoanApplicationStatus() != LoanApplicationStatus.IN_REVIEW) {
            throw new InvalidApplicationStateException("Only applications in IN_REVIEW status can be validated.");
        }

        loanApplicationMapper.updateFromRegenerateRequest(request, application);

        PaymentSchedule newSchedule = paymentScheduleGenerator.generateSchedule(application);
        newSchedule.setLoanApplication(application);
        application.setPaymentSchedule(newSchedule);

        loanApplicationRepository.save(application);
    }

    /**
     * Approves a loan application with the given ID.
     *
     * @param id The ID of the loan application to approve.
     */
    @Transactional
    public void approveLoanApplication(Long id) {
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "approve");
        application.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
    }

    /**
     * Rejects a loan application with the given ID and reason.
     *
     * @param id     The ID of the loan application to reject.
     * @param reason The reason for rejecting the loan application.
     */
    @Transactional
    public void rejectLoanApplication(Long id, LoanRejectionReason reason) {
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "reject");
        application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        loanApplicationRepository.save(application);
    }

    /**
     * Retrieves a loan application by its ID. If the application is not found, an exception is thrown.
     *
     * @param id The ID of the loan application to retrieve.
     * @return The loan application with the specified ID.
     */
    private LoanApplication getApplicationOrThrow(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application with ID " + id + " not found."));
    }

    /**
     * Asserts that the loan application is in the IN_REVIEW status. If it is not, an exception is thrown with a message indicating the allowed status and the attempted action.
     *
     * @param application The loan application to check.
     * @param action      The action being attempted (e.g., "approve" or "reject") for error message clarity.
     */
    private void assertInReview(LoanApplication application, String action) {
        if (application.getLoanApplicationStatus() != LoanApplicationStatus.IN_REVIEW) {
            throw new InvalidApplicationStateException(
                    "Cannot " + action + " application with status: "
                            + application.getLoanApplicationStatus()
                            + ". Only applications in IN_REVIEW status can be " + action + "."
            );
        }
    }
}
