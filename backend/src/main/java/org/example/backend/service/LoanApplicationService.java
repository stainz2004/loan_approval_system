package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.dto.ValidationDecision;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.example.backend.repository.PaymentScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public List<LoanApplicationResponse> getLoanApplications() {
        return null;
    }
}
