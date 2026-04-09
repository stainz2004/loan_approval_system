package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.dto.ValidationDecision;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
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
    public ValidationDecision createLoanApplication(LoanApplicationRequest request) {
        LoanApplication application = loanApplicationMapper.toEntity(request);

        loanApplicationRepository.save(application);

        ValidationDecision decision = loanApplicationValidator.validateCreateRequest(request);

        if (!decision.isAccepted()) {
            application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
            application.setRejectionReason(decision.getRejectionReason());
            return decision;
        }

        PaymentSchedule schedule = paymentScheduleGenerator.generateSchedule(application);
        paymentScheduleRepository.save(schedule);

        application.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        return decision;
    }

    @Transactional
    public void approveLoanApplication(Long id) {
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "approve");
        application.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
    }

    @Transactional
    public void rejectLoanApplication(Long id, String reason) {
        LoanApplication application = getApplicationOrThrow(id);
        assertInReview(application, "reject");
        application.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        loanApplicationRepository.save(application);
    }

    private LoanApplication getApplicationOrThrow(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with id: " + id));
    }

    private void assertInReview(LoanApplication application, String action) {
        if (application.getLoanApplicationStatus() != LoanApplicationStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "Cannot " + action + " application with status: "
                            + application.getLoanApplicationStatus()
                            + ". Only applications in IN_REVIEW status can be " + action + "."
            );
        }
    }
}
