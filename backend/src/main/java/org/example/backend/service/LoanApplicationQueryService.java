package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentScheduleItem;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.example.backend.repository.PaymentScheduleItemRepository;
import org.example.backend.repository.PaymentScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationQueryService {


    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final PaymentScheduleItemRepository paymentScheduleItemRepository;

    public List<LoanApplicationResponse> getLoanApplications() {
        List<LoanApplication> applications =
                loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);

        return applications.stream()
                .map(this::toLoanApplicationResponse)
                .toList();
    }

    private LoanApplicationResponse toLoanApplicationResponse(LoanApplication application) {
        List<PaymentScheduleItem> items = getPaymentScheduleItems(application);
        return loanApplicationMapper.toResponse(application, items);
    }

    private List<PaymentScheduleItem> getPaymentScheduleItems(LoanApplication application) {
        return paymentScheduleRepository.findByLoanApplication(application)
                .map(paymentScheduleItemRepository::findByPaymentSchedule)
                .orElse(List.of());
    }
}
