package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
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
    public void createLoanApplication(LoanApplicationRequest request) {
        loanApplicationValidator.validateCreateRequest(request);

        LoanApplication application = loanApplicationMapper.toEntity(request);

        loanApplicationRepository.save(application);

        PaymentSchedule schedule = paymentScheduleGenerator.generateSchedule(application);
        paymentScheduleRepository.save(schedule);

        application.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        loanApplicationRepository.save(application);
    }

    public List<LoanApplicationResponse> getLoanApplications() {
        // TODO
    }
}
