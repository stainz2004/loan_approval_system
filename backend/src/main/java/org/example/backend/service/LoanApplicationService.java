package org.example.backend.service;

import org.example.backend.dto.LoanApplicationCreationResponse;
import org.example.backend.dto.PaymentScheduleItemDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.entity.LoanApplicationStatus;
import org.example.backend.entity.LoanRejectionReason;
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

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationValidator loanApplicationValidator;
    private final PaymentScheduleGenerator paymentScheduleGenerator;
    private final LoanApplicationMapper loanApplicationMapper;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final LoanConfigService loanConfigService;

    private static final String UNIQUE_IN_REVIEW_CONSTRAINT =
            "uq_loan_application_personal_code_in_review";

    /**
     * Creates a new loan application based on the provided request. This method validates the request,
     *
     * @param request The loan application request containing the necessary information to create a loan application.
     */
    @Transactional
    public LoanApplicationCreationResponse createLoanApplication(LoanApplicationRequest request) {
        loanApplicationValidator.validateCustomerPersonalCode(request.personalCode());

        if (loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                request.personalCode(), LoanApplicationStatus.IN_REVIEW)) {
            throw new ActiveApplicationExistsException();
        }

        LoanApplicationDecisionResponse decision = loanApplicationValidator.validateAge(request.personalCode());

        LoanApplication application = loanApplicationMapper.toEntity(request);
        application.setBaseInterest(loanConfigService.getBaseInterest());

        if (!decision.isAccepted()) {
            application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
            application.setRejectionReason(decision.rejectionReason());
            loanApplicationRepository.save(application);
            log.info("Loan application id={} rejected at creation. Reason: {}",
                    application.getId(), decision.rejectionReason());
            return LoanApplicationCreationResponse.rejected(decision.rejectionReason());
        }

        application.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        try {
            loanApplicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            if (isUniqueInReviewViolation(ex)) {
                throw new ActiveApplicationExistsException();
            }
            throw ex;
        }

        log.info("Loan application id={} created with IN_REVIEW status", application.getId());
        PaymentSchedule schedule = paymentScheduleGenerator.generateSchedule(application);
        paymentScheduleRepository.save(schedule);
        List<PaymentScheduleItemDTO> items = loanApplicationMapper.toPaymentScheduleItems(schedule.getItems());
        return LoanApplicationCreationResponse.accepted(items);
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
        log.info("Regenerating payment schedule for application id={}", id);
        LoanApplication application = loanApplicationRepository.findByIdWithSchedule(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "Loan application with ID " + id + " not found."));

        if (application.getLoanApplicationStatus() != LoanApplicationStatus.IN_REVIEW) {
            throw new InvalidApplicationStateException("Only applications in IN_REVIEW status can be regenerated.");
        }

        loanApplicationMapper.updateFromRegenerateRequest(request, application);
        application.setBaseInterest(loanConfigService.getBaseInterest());

        PaymentSchedule newSchedule = paymentScheduleGenerator.generateSchedule(application);
        PaymentSchedule existingSchedule = application.getPaymentSchedule();

        if (existingSchedule == null) {
            newSchedule.setLoanApplication(application);
            application.setPaymentSchedule(newSchedule);
            paymentScheduleRepository.save(newSchedule);
        } else {
            existingSchedule.getItems().clear();
            newSchedule.getItems().forEach(item -> item.setPaymentSchedule(existingSchedule));
            existingSchedule.getItems().addAll(newSchedule.getItems());
        }

        loanApplicationRepository.save(application);
    }

    /**
     * Approves a loan application with the given ID.
     *
     * @param id The ID of the loan application to approve.
     */
    @Transactional
    public void approveLoanApplication(Long id) {
        log.info("Approving loan application id={}", id);
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "approve");
        application.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
        log.info("Loan application id={} approved", id);
    }

    /**
     * Rejects a loan application with the given ID and reason.
     *
     * @param id     The ID of the loan application to reject.
     * @param reason The reason for rejecting the loan application.
     */
    @Transactional
    public void rejectLoanApplication(Long id, LoanRejectionReason reason) {
        log.info("Rejecting loan application id={} with reason: {}", id, reason);
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "reject");
        application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        loanApplicationRepository.save(application);
        log.info("Loan application id={} rejected", id);
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

    /**
     * Checks if the given DataIntegrityViolationException was caused by a unique constraint violation on the personal code for applications in IN_REVIEW status.
     *
     * @param e The DataIntegrityViolationException to check.
     * @return true if the exception was caused by a unique constraint violation on the personal code for IN_REVIEW applications, false otherwise.
     */
    private boolean isUniqueInReviewViolation(DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause().getMessage();
        return msg != null && msg.contains(UNIQUE_IN_REVIEW_CONSTRAINT);
    }
}
