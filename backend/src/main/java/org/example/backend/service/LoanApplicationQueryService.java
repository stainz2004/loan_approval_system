package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentScheduleItem;
import org.example.backend.exception.LoanApplicationNotFoundException;
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
    private final LoanApplicationValidator loanApplicationValidator;

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
     * Retrieves the payment schedule for a loan application based on the provided personal code. This method first validates the personal code,
     * then finds the loan application with the "IN_REVIEW" status associated with that personal code.
     *
     * @param personalCode The personal code of the customer for whom to retrieve the loan application and its payment schedule.
     * @return A LoanApplicationResponse containing the details of the loan application and its associated payment schedule items.
     */
    public LoanApplicationResponse getPaymentScheduleByPersonalCode(String personalCode) {
        loanApplicationValidator.validateCustomerPersonalCode(personalCode);

        LoanApplication application = loanApplicationRepository.findByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.IN_REVIEW);

        if (application == null) {
            throw new LoanApplicationNotFoundException("No loan application found for the provided personal code.");
        }

        List<PaymentScheduleItem> items = getPaymentScheduleItems(application);

        return loanApplicationMapper.toResponse(application, items);
    }

    /**
     * Retrieves a list of approved loan applications for a given personal code. This method validates the personal code and then queries the repository
     * for all loan applications associated with that personal code that have the "APPROVED" status.
     *
     * @param personalCode The personal code of the customer for whom to retrieve the approved loan applications.
     * @return A list of LoanApplicationResponse objects representing the approved loan applications for the specified personal code.
     */
    public List<LoanApplicationResponse> getApprovedLoanApplications(String personalCode) {
        loanApplicationValidator.validateCustomerPersonalCode(personalCode);

        List<LoanApplication> applications = loanApplicationRepository.findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED);

        if (applications.isEmpty()) {
            throw new LoanApplicationNotFoundException("No approved loan applications found for the provided personal code.");
        }

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
