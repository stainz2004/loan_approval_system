package org.example.backend.service;

import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentScheduleGeneratorTest {

    @Mock
    private LoanConfigService loanConfigService;

    private PaymentScheduleGenerator generator;

    private static final BigDecimal DEFAULT_BASE_INTEREST = new BigDecimal("3.0");

    @BeforeEach
    void setUp() {
        generator = new PaymentScheduleGenerator(loanConfigService);
    }

    private LoanApplication buildApplication(BigDecimal loanAmount, int periodMonths,
                                              BigDecimal interestMargin) {
        LoanApplication app = new LoanApplication();
        app.setLoanAmount(loanAmount);
        app.setLoanPeriodMonths(periodMonths);
        app.setInterestMargin(interestMargin);
        return app;
    }

    @Test
    void generateSchedule_returnsScheduleLinkedToApplication() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        assertThat(schedule).isNotNull();
        assertThat(schedule.getLoanApplication()).isSameAs(app);
    }

    @Test
    void generateSchedule_returnsCorrectNumberOfItems() {
        int months = 24;
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), months,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        assertThat(schedule.getItems()).hasSize(months);
    }

    @Test
    void generateSchedule_itemsHaveSequentialPaymentNumbers() {
        int months = 6;
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), months,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);
        List<PaymentScheduleItem> items = schedule.getItems();

        for (int i = 0; i < months; i++) {
            assertThat(items.get(i).getPaymentNumber()).isEqualTo(i + 1);
        }
    }

    @Test
    void generateSchedule_firstItemDueDateIsToday() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        assertThat(schedule.getItems().get(0).getDueDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void generateSchedule_dueDatesAreOneMonthApart() {
        int months = 6;
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), months,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);
        List<PaymentScheduleItem> items = schedule.getItems();

        for (int i = 1; i < months; i++) {
            assertThat(items.get(i).getDueDate())
                    .isEqualTo(items.get(i - 1).getDueDate().plusMonths(1));
        }
    }

    @Test
    void generateSchedule_lastItemRemainingBalanceIsZero() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);
        PaymentScheduleItem lastItem = schedule.getItems().get(11);

        assertThat(lastItem.getRemainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void generateSchedule_allItemsHavePositiveTotalAmount() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        schedule.getItems().forEach(item ->
                assertThat(item.getTotalAmount()).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    void generateSchedule_allItemsHaveNonNegativeRemainingBalance() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        schedule.getItems().forEach(item ->
                assertThat(item.getRemainingBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO));
    }

    @Test
    void generateSchedule_allItemsHaveTotalAmountScaledToTwoDecimalPlaces() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);

        schedule.getItems().forEach(item ->
                assertThat(item.getTotalAmount().scale()).isLessThanOrEqualTo(2));
    }

    @Test
    void generateSchedule_withVeryLowInterest_lastItemBalanceIsZero() {
        BigDecimal loanAmount = new BigDecimal("12000");
        int months = 12;
        LoanApplication app = buildApplication(loanAmount, months,
                new BigDecimal("0.01"));
        when(loanConfigService.getBaseInterest()).thenReturn(new BigDecimal("0.01"));

        PaymentSchedule schedule = generator.generateSchedule(app);
        List<PaymentScheduleItem> items = schedule.getItems();

        assertThat(items.get(months - 1).getRemainingBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
        items.forEach(item -> assertThat(item.getTotalAmount()).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    void generateSchedule_remainingBalanceDecreasesMonthToMonth() {
        LoanApplication app = buildApplication(
                new BigDecimal("10000"), 12,
                new BigDecimal("2.0"));
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);
        List<PaymentScheduleItem> items = schedule.getItems();

        for (int i = 1; i < items.size(); i++) {
            assertThat(items.get(i).getRemainingBalance())
                    .isLessThan(items.get(i - 1).getRemainingBalance());
        }
    }

    @Test
    void generateSchedule_singleMonthLoan_totalAmountEqualsLoanAmountPlusInterest() {
        BigDecimal loanAmount = new BigDecimal("10000");
        BigDecimal interestMargin = new BigDecimal("2.0");
        LoanApplication app = buildApplication(loanAmount, 1, interestMargin);
        when(loanConfigService.getBaseInterest()).thenReturn(DEFAULT_BASE_INTEREST);

        PaymentSchedule schedule = generator.generateSchedule(app);
        PaymentScheduleItem item = schedule.getItems().get(0);

        assertThat(item.getTotalAmount()).isGreaterThan(loanAmount);
        assertThat(item.getRemainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
