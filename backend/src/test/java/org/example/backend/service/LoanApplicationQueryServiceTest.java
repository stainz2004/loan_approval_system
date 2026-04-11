package org.example.backend.service;

import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
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
import static org.mockito.Mockito.mock;
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
}
