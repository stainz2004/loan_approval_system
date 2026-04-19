package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.entity.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentScheduleItem;
import org.example.backend.exception.LoanApplicationNotFoundException;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationQueryService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanApplicationValidator loanApplicationValidator;

    /**
     * Retrieves a list of loan applications that are currently in the "IN_REVIEW" status. Each application is mapped to a response DTO
     * that includes the application details and its associated payment schedule items.
     *
     * @return A list of LoanApplicationResponse objects representing the loan applications in review.
     */
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplications() {
        List<LoanApplication> applications =
                loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);

        return applications.stream()
                .map(this::toLoanApplicationResponse)
                .toList();
    }

    /**
     * Retrieves the payment schedule for a loan application with the given ID that is currently in IN_REVIEW status.
     *
     * @param id The ID of the loan application for which to retrieve the payment schedule.
     * @return A LoanApplicationResponse containing the details of the loan application and its associated payment schedule items.
     */
    @Transactional(readOnly = true)
    public LoanApplicationResponse getPaymentScheduleById(Long id) {
        LoanApplication application = loanApplicationRepository
                .findByIdAndLoanApplicationStatus(id, LoanApplicationStatus.IN_REVIEW)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "No IN_REVIEW loan application found with ID: " + id));

        return loanApplicationMapper.toResponse(application, getPaymentScheduleItems(application));
    }

    /**
     * Retrieves a list of approved loan applications for a given personal code. This method validates the personal code and then queries the repository
     * for all loan applications associated with that personal code that have the "APPROVED" status.
     *
     * @param personalCode The personal code of the customer for whom to retrieve the approved loan applications.
     * @return A list of LoanApplicationResponse objects representing the approved loan applications for the specified personal code.
     */
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getApprovedLoanApplications(String personalCode) {
        loanApplicationValidator.validateCustomerPersonalCode(personalCode);

        List<LoanApplication> applications = loanApplicationRepository
                .findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED);

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
        return loanApplicationMapper.toResponse(application, getPaymentScheduleItems(application));
    }

    /**
     * Retrieves the payment schedule items associated with a given loan application directly via the entity relationship,
     * avoiding extra repository queries.
     *
     * @param application The LoanApplication entity for which to retrieve the payment schedule items.
     * @return A list of PaymentScheduleItem entities associated with the loan application, or an empty list if no schedule is found.
     */
    private List<PaymentScheduleItem> getPaymentScheduleItems(LoanApplication application) {
        if (application.getPaymentSchedule() == null) {
            return List.of();
        }
        return application.getPaymentSchedule().getItems();
    }
}
