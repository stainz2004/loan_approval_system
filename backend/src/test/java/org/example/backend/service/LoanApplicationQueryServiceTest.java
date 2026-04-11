package org.example.backend.service;

import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.example.backend.exception.InvalidPersonalCodeException;
import org.example.backend.exception.LoanApplicationNotFoundException;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.example.backend.repository.PaymentScheduleItemRepository;
import org.example.backend.repository.PaymentScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationQueryServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationMapper loanApplicationMapper;

    @Mock
    private PaymentScheduleRepository paymentScheduleRepository;

    @Mock
    private PaymentScheduleItemRepository paymentScheduleItemRepository;

    @Mock
    private LoanApplicationValidator loanApplicationValidator;

    @InjectMocks
    private LoanApplicationQueryService queryService;


    @Test
    void getLoanApplications_returnsEmptyList_whenNoInReviewApplicationsExist() {
        when(loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW))
                .thenReturn(List.of());

        List<LoanApplicationResponse> result = queryService.getLoanApplications();

        assertThat(result).isEmpty();
    }

    @Test
    void getLoanApplications_returnsMappedResponses_forEachInReviewApplication() {
        LoanApplication app1 = buildApplication(1L);
        LoanApplication app2 = buildApplication(2L);

        PaymentSchedule schedule1 = new PaymentSchedule();
        PaymentSchedule schedule2 = new PaymentSchedule();

        PaymentScheduleItem item1 = new PaymentScheduleItem();
        PaymentScheduleItem item2 = new PaymentScheduleItem();

        LoanApplicationResponse response1 = mock(LoanApplicationResponse.class);
        LoanApplicationResponse response2 = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW))
                .thenReturn(List.of(app1, app2));

        when(paymentScheduleRepository.findByLoanApplication(app1)).thenReturn(Optional.of(schedule1));
        when(paymentScheduleRepository.findByLoanApplication(app2)).thenReturn(Optional.of(schedule2));

        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule1)).thenReturn(List.of(item1));
        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule2)).thenReturn(List.of(item2));

        when(loanApplicationMapper.toResponse(app1, List.of(item1))).thenReturn(response1);
        when(loanApplicationMapper.toResponse(app2, List.of(item2))).thenReturn(response2);

        List<LoanApplicationResponse> result = queryService.getLoanApplications();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void getLoanApplications_returnsEmptyScheduleItems_whenNoScheduleExistsForApplication() {
        LoanApplication app = buildApplication(1L);
        LoanApplicationResponse response = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW))
                .thenReturn(List.of(app));
        when(paymentScheduleRepository.findByLoanApplication(app)).thenReturn(Optional.empty());
        when(loanApplicationMapper.toResponse(app, List.of())).thenReturn(response);

        List<LoanApplicationResponse> result = queryService.getLoanApplications();

        assertThat(result).containsExactly(response);
        verify(loanApplicationMapper).toResponse(app, List.of());
    }

    @Test
    void getLoanApplications_queriesOnlyInReviewStatus() {
        when(loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW))
                .thenReturn(List.of());

        queryService.getLoanApplications();

        verify(loanApplicationRepository).findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
    }

    @Test
    void getLoanApplications_mapsAllScheduleItemsForApplication() {
        LoanApplication app = buildApplication(1L);
        PaymentSchedule schedule = new PaymentSchedule();

        PaymentScheduleItem item1 = new PaymentScheduleItem();
        PaymentScheduleItem item2 = new PaymentScheduleItem();
        PaymentScheduleItem item3 = new PaymentScheduleItem();
        List<PaymentScheduleItem> items = List.of(item1, item2, item3);

        LoanApplicationResponse response = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW))
                .thenReturn(List.of(app));
        when(paymentScheduleRepository.findByLoanApplication(app)).thenReturn(Optional.of(schedule));
        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule)).thenReturn(items);
        when(loanApplicationMapper.toResponse(app, items)).thenReturn(response);

        List<LoanApplicationResponse> result = queryService.getLoanApplications();

        assertThat(result).hasSize(1);
        verify(loanApplicationMapper).toResponse(app, items);
    }

    // -------------------------------------------------------------------------
    // getPaymentScheduleById
    // -------------------------------------------------------------------------

    @Test
    void getPaymentScheduleById_returnsResponse_whenApplicationFoundWithScheduleItems() {
        LoanApplication app = buildApplication(1L);
        PaymentSchedule schedule = new PaymentSchedule();
        PaymentScheduleItem item = new PaymentScheduleItem();
        LoanApplicationResponse expected = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByIdAndLoanApplicationStatus(1L, LoanApplicationStatus.IN_REVIEW))
                .thenReturn(app);
        when(paymentScheduleRepository.findByLoanApplication(app)).thenReturn(Optional.of(schedule));
        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule)).thenReturn(List.of(item));
        when(loanApplicationMapper.toResponse(app, List.of(item))).thenReturn(expected);

        LoanApplicationResponse result = queryService.getPaymentScheduleById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPaymentScheduleById_returnsResponseWithEmptyItems_whenNoScheduleExists() {
        LoanApplication app = buildApplication(1L);
        LoanApplicationResponse expected = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByIdAndLoanApplicationStatus(1L, LoanApplicationStatus.IN_REVIEW))
                .thenReturn(app);
        when(paymentScheduleRepository.findByLoanApplication(app)).thenReturn(Optional.empty());
        when(loanApplicationMapper.toResponse(app, List.of())).thenReturn(expected);

        LoanApplicationResponse result = queryService.getPaymentScheduleById(1L);

        assertThat(result).isEqualTo(expected);
        verify(loanApplicationMapper).toResponse(app, List.of());
    }

    @Test
    void getPaymentScheduleById_throwsLoanApplicationNotFoundException_whenApplicationNotFound() {
        when(loanApplicationRepository.findByIdAndLoanApplicationStatus(99L, LoanApplicationStatus.IN_REVIEW))
                .thenReturn(null);

        assertThatThrownBy(() -> queryService.getPaymentScheduleById(99L))
                .isInstanceOf(LoanApplicationNotFoundException.class)
                .hasMessageContaining("No loan application found for the provided personal code.");
    }

    @Test
    void getPaymentScheduleById_queriesRepositoryWithCorrectIdAndInReviewStatus() {
        LoanApplication app = buildApplication(5L);
        LoanApplicationResponse expected = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findByIdAndLoanApplicationStatus(5L, LoanApplicationStatus.IN_REVIEW))
                .thenReturn(app);
        when(paymentScheduleRepository.findByLoanApplication(app)).thenReturn(Optional.empty());
        when(loanApplicationMapper.toResponse(app, List.of())).thenReturn(expected);

        queryService.getPaymentScheduleById(5L);

        verify(loanApplicationRepository).findByIdAndLoanApplicationStatus(5L, LoanApplicationStatus.IN_REVIEW);
    }

    // -------------------------------------------------------------------------
    // getApprovedLoanApplications
    // -------------------------------------------------------------------------

    @Test
    void getApprovedLoanApplications_returnsMappedResponses_forApprovedApplications() {
        String personalCode = "38001085718";
        LoanApplication app1 = buildApprovedApplication(10L, personalCode);
        LoanApplication app2 = buildApprovedApplication(11L, personalCode);
        PaymentSchedule schedule1 = new PaymentSchedule();
        PaymentSchedule schedule2 = new PaymentSchedule();
        PaymentScheduleItem item1 = new PaymentScheduleItem();
        PaymentScheduleItem item2 = new PaymentScheduleItem();
        LoanApplicationResponse response1 = mock(LoanApplicationResponse.class);
        LoanApplicationResponse response2 = mock(LoanApplicationResponse.class);

        when(loanApplicationRepository.findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED))
                .thenReturn(List.of(app1, app2));
        when(paymentScheduleRepository.findByLoanApplication(app1)).thenReturn(Optional.of(schedule1));
        when(paymentScheduleRepository.findByLoanApplication(app2)).thenReturn(Optional.of(schedule2));
        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule1)).thenReturn(List.of(item1));
        when(paymentScheduleItemRepository.findByPaymentSchedule(schedule2)).thenReturn(List.of(item2));
        when(loanApplicationMapper.toResponse(app1, List.of(item1))).thenReturn(response1);
        when(loanApplicationMapper.toResponse(app2, List.of(item2))).thenReturn(response2);

        List<LoanApplicationResponse> result = queryService.getApprovedLoanApplications(personalCode);

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void getApprovedLoanApplications_throwsLoanApplicationNotFoundException_whenNoApprovedApplicationsExist() {
        String personalCode = "38001085718";

        when(loanApplicationRepository.findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> queryService.getApprovedLoanApplications(personalCode))
                .isInstanceOf(LoanApplicationNotFoundException.class)
                .hasMessageContaining("No approved loan applications found for the provided personal code.");
    }

    @Test
    void getApprovedLoanApplications_throwsInvalidPersonalCodeException_whenPersonalCodeIsInvalid() {
        String invalidCode = "00000000000";

        doThrow(new InvalidPersonalCodeException("Invalid personal code!"))
                .when(loanApplicationValidator).validateCustomerPersonalCode(invalidCode);

        assertThatThrownBy(() -> queryService.getApprovedLoanApplications(invalidCode))
                .isInstanceOf(InvalidPersonalCodeException.class)
                .hasMessageContaining("Invalid personal code!");

        verify(loanApplicationRepository, never())
                .findAllByPersonalCodeAndLoanApplicationStatus(invalidCode, LoanApplicationStatus.APPROVED);
    }

    @Test
    void getApprovedLoanApplications_validatesPersonalCodeBeforeQueryingRepository() {
        String personalCode = "38001085718";

        when(loanApplicationRepository.findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED))
                .thenReturn(List.of(buildApprovedApplication(1L, personalCode)));
        when(paymentScheduleRepository.findByLoanApplication(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());
        when(loanApplicationMapper.toResponse(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(mock(LoanApplicationResponse.class));

        queryService.getApprovedLoanApplications(personalCode);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(loanApplicationValidator, loanApplicationRepository);
        inOrder.verify(loanApplicationValidator).validateCustomerPersonalCode(personalCode);
        inOrder.verify(loanApplicationRepository).findAllByPersonalCodeAndLoanApplicationStatus(personalCode, LoanApplicationStatus.APPROVED);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private LoanApplication buildApplication(Long id) {
        LoanApplication app = new LoanApplication();
        app.setId(id);
        app.setFirstName("John");
        app.setLastName("Doe");
        app.setPersonalCode("38001085718");
        app.setLoanAmount(new BigDecimal("10000"));
        app.setLoanPeriodMonths(12);
        app.setInterestMargin(new BigDecimal("2.0"));
        app.setBaseInterest(new BigDecimal("3.0"));
        app.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        return app;
    }

    private LoanApplication buildApprovedApplication(Long id, String personalCode) {
        LoanApplication app = new LoanApplication();
        app.setId(id);
        app.setFirstName("Jane");
        app.setLastName("Smith");
        app.setPersonalCode(personalCode);
        app.setLoanAmount(new BigDecimal("5000"));
        app.setLoanPeriodMonths(24);
        app.setInterestMargin(new BigDecimal("1.5"));
        app.setBaseInterest(new BigDecimal("2.5"));
        app.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        return app;
    }
}
