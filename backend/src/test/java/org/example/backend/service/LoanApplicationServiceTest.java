package org.example.backend.service;

import org.example.backend.dto.LoanApplicationCreationResponse;
import org.example.backend.dto.LoanApplicationDecisionResponse;
import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationStatus;
import org.example.backend.dto.LoanRejectionReason;
import org.example.backend.dto.RegenerateScheduleRequest;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.exception.ActiveApplicationExistsException;
import org.example.backend.exception.InvalidApplicationStateException;
import org.example.backend.exception.LoanApplicationNotFoundException;
import org.example.backend.mapper.LoanApplicationMapper;
import org.example.backend.repository.LoanApplicationRepository;
import org.example.backend.repository.PaymentScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationValidator loanApplicationValidator;

    @Mock
    private PaymentScheduleGenerator paymentScheduleGenerator;

    @Mock
    private LoanApplicationMapper loanApplicationMapper;

    @Mock
    private PaymentScheduleRepository paymentScheduleRepository;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private LoanApplicationRequest validRequest;
    private LoanApplication mappedApplication;
    private PaymentSchedule generatedSchedule;

    @BeforeEach
    void setUp() {
        validRequest = new LoanApplicationRequest(
                "John", "Doe", "38001085718",
                12, new BigDecimal("2.0"), new BigDecimal("3.0"), new BigDecimal("10000"));

        mappedApplication = new LoanApplication();
        mappedApplication.setPersonalCode("38001085718");

        generatedSchedule = new PaymentSchedule();
    }


    @Test
    void createLoanApplication_throwsActiveApplicationExistsException_whenActiveApplicationExists() {
        when(loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                "38001085718", LoanApplicationStatus.IN_REVIEW)).thenReturn(true);

        assertThatThrownBy(() -> loanApplicationService.createLoanApplication(validRequest))
                .isInstanceOf(ActiveApplicationExistsException.class);

        verify(loanApplicationValidator, never()).validateCustomerPersonalCode(any());
    }

    @Test
    void createLoanApplication_savesRejectedApplication_whenAgeValidationFails() {
        when(loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                "38001085718", LoanApplicationStatus.IN_REVIEW)).thenReturn(false);
        when(loanApplicationMapper.toEntity(validRequest)).thenReturn(mappedApplication);
        when(loanApplicationValidator.validateAge("38001085718"))
                .thenReturn(LoanApplicationDecisionResponse.rejected(LoanRejectionReason.CUSTOMER_TOO_OLD));

        LoanApplicationCreationResponse response = loanApplicationService.createLoanApplication(validRequest);

        assertThat(response.isAccepted()).isFalse();
        assertThat(response.rejectionReason()).isEqualTo(LoanRejectionReason.CUSTOMER_TOO_OLD);

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getLoanApplicationStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(captor.getValue().getRejectionReason()).isEqualTo(LoanRejectionReason.CUSTOMER_TOO_OLD);

        verify(paymentScheduleGenerator, never()).generateSchedule(any());
        verify(paymentScheduleRepository, never()).save(any());
    }

    @Test
    void createLoanApplication_savesInReviewApplicationAndSchedule_whenValidationPasses() {
        when(loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                "38001085718", LoanApplicationStatus.IN_REVIEW)).thenReturn(false);
        when(loanApplicationMapper.toEntity(validRequest)).thenReturn(mappedApplication);
        when(loanApplicationValidator.validateAge("38001085718"))
                .thenReturn(LoanApplicationDecisionResponse.accepted());
        when(paymentScheduleGenerator.generateSchedule(mappedApplication)).thenReturn(generatedSchedule);

        LoanApplicationCreationResponse response = loanApplicationService.createLoanApplication(validRequest);

        assertThat(response.isAccepted()).isTrue();

        ArgumentCaptor<LoanApplication> appCaptor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(appCaptor.capture());
        assertThat(appCaptor.getValue().getLoanApplicationStatus()).isEqualTo(LoanApplicationStatus.IN_REVIEW);

        verify(paymentScheduleRepository).save(generatedSchedule);
    }

    @Test
    void createLoanApplication_callsValidatePersonalCode() {
        when(loanApplicationRepository.existsByPersonalCodeAndLoanApplicationStatus(
                "38001085718", LoanApplicationStatus.IN_REVIEW)).thenReturn(false);
        when(loanApplicationMapper.toEntity(validRequest)).thenReturn(mappedApplication);
        when(loanApplicationValidator.validateAge("38001085718"))
                .thenReturn(LoanApplicationDecisionResponse.accepted());
        when(paymentScheduleGenerator.generateSchedule(any())).thenReturn(generatedSchedule);

        loanApplicationService.createLoanApplication(validRequest);

        verify(loanApplicationValidator).validateCustomerPersonalCode("38001085718");
    }


    @Test
    void regenerateSchedule_throwsLoanApplicationNotFoundException_whenApplicationNotFound() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.regenerateSchedule(99L,
                new RegenerateScheduleRequest(new BigDecimal("10000"), new BigDecimal("2.0"),
                        new BigDecimal("3.0"), 12)))
                .isInstanceOf(LoanApplicationNotFoundException.class);
    }

    @Test
    void regenerateSchedule_throwsInvalidApplicationStateException_whenApplicationIsNotInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> loanApplicationService.regenerateSchedule(1L,
                new RegenerateScheduleRequest(new BigDecimal("10000"), new BigDecimal("2.0"),
                        new BigDecimal("3.0"), 12)))
                .isInstanceOf(InvalidApplicationStateException.class);
    }

    @Test
    void regenerateSchedule_savesApplicationWithNewSchedule_whenApplicationIsInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        app.setLoanAmount(new BigDecimal("10000"));
        app.setLoanPeriodMonths(12);
        app.setInterestMargin(new BigDecimal("2.0"));
        app.setBaseInterest(new BigDecimal("3.0"));

        RegenerateScheduleRequest request = new RegenerateScheduleRequest(
                new BigDecimal("15000"), new BigDecimal("1.5"), new BigDecimal("2.5"), 24);

        PaymentSchedule newSchedule = new PaymentSchedule();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(paymentScheduleGenerator.generateSchedule(app)).thenReturn(newSchedule);

        loanApplicationService.regenerateSchedule(1L, request);

        verify(loanApplicationMapper).updateFromRegenerateRequest(request, app);
        verify(loanApplicationRepository).save(app);
        assertThat(app.getPaymentSchedule()).isSameAs(newSchedule);
        assertThat(newSchedule.getLoanApplication()).isSameAs(app);
    }


    @Test
    void approveLoanApplication_throwsLoanApplicationNotFoundException_whenApplicationNotFound() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.approveLoanApplication(99L))
                .isInstanceOf(LoanApplicationNotFoundException.class);
    }

    @Test
    void approveLoanApplication_throwsInvalidApplicationStateException_whenApplicationIsNotInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> loanApplicationService.approveLoanApplication(1L))
                .isInstanceOf(InvalidApplicationStateException.class);
    }

    @Test
    void approveLoanApplication_setsStatusToApproved_whenApplicationIsInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        loanApplicationService.approveLoanApplication(1L);

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getLoanApplicationStatus()).isEqualTo(LoanApplicationStatus.APPROVED);
    }


    @Test
    void rejectLoanApplication_throwsLoanApplicationNotFoundException_whenApplicationNotFound() {
        when(loanApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.rejectLoanApplication(99L, LoanRejectionReason.OTHER))
                .isInstanceOf(LoanApplicationNotFoundException.class);
    }

    @Test
    void rejectLoanApplication_throwsInvalidApplicationStateException_whenApplicationIsNotInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> loanApplicationService.rejectLoanApplication(1L, LoanRejectionReason.OTHER))
                .isInstanceOf(InvalidApplicationStateException.class);
    }

    @Test
    void rejectLoanApplication_setsStatusToRejectedWithReason_whenApplicationIsInReview() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.IN_REVIEW);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        loanApplicationService.rejectLoanApplication(1L, LoanRejectionReason.OTHER);

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getLoanApplicationStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(captor.getValue().getRejectionReason()).isEqualTo(LoanRejectionReason.OTHER);
    }

    @Test
    void rejectLoanApplication_throwsInvalidApplicationStateException_whenApplicationIsApproved() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> loanApplicationService.rejectLoanApplication(1L, LoanRejectionReason.INSUFFICIENT_DATA))
                .isInstanceOf(InvalidApplicationStateException.class)
                .hasMessageContaining("Cannot reject");
    }
}
