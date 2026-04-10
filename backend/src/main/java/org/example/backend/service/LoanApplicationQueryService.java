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

    /**
     * Retrieves a list of loan applications that are currently in the "IN_REVIEW" status. Each application is mapped to a response DTO
     * that includes the application details and its associated payment schedule items.
     *
     * @return A list of LoanApplicationResponse objects representing the loan applications in review.
     */
    public List<LoanApplicationResponse> getLoanApplications() {
        List<LoanApplication> applications =
                loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);

        return applications.stream()
                .map(this::toLoanApplicationResponse)
                .toList();
    }

    /**
     * Converts a LoanApplication entity to a LoanApplicationResponse DTO. This method retrieves the associated payment schedule items for the given application
     * and includes them in the response.
     *
     * @param application The LoanApplication entity to be converted.
     * @return A LoanApplicationResponse DTO containing the application details and its payment schedule items.
     */
    private LoanApplicationResponse toLoanApplicationResponse(LoanApplication application) {
        List<PaymentScheduleItem> items = getPaymentScheduleItems(application);
        return loanApplicationMapper.toResponse(application, items);
    }

    /**
     * Retrieves the payment schedule items associated with a given loan application. This method first finds the payment schedule for the application
     * and then retrieves the items linked to that schedule.
     *
     * @param application The LoanApplication entity for which to retrieve the payment schedule items.
     * @return A list of PaymentScheduleItem entities associated with the loan application, or an empty list if no schedule is found.
     */
    private List<PaymentScheduleItem> getPaymentScheduleItems(LoanApplication application) {
        return paymentScheduleRepository.findByLoanApplication(application)
                .map(paymentScheduleItemRepository::findByPaymentSchedule)
                .orElse(List.of());
    }
}
