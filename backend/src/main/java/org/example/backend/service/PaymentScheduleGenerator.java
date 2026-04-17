package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates an annuity-based payment schedule for a loan application.
 *
 * <p>Each instalment consists of a fixed total payment (principal + interest),
 * except the final payment which is adjusted to clear any remaining balance
 * caused by rounding throughout the schedule.
 */
@Service
@RequiredArgsConstructor
public class PaymentScheduleGenerator {

    // Money values are rounded to cents
    private static final int CURRENCY_DECIMAL_PLACES = 2;
    private static final int RATE_DECIMAL_PLACES = 10;
    private static final BigDecimal ANNUAL_TO_MONTHLY_DIVISOR = BigDecimal.valueOf(1200);

    // Use the LoanConfigService to get the base interest rate for schedule generation
    private final LoanConfigService loanConfigService;

    /**
     * Generates a payment schedule for a given loan application.
     *
     * @param loanApplication the loan application for which to generate the schedule
     * @return a {@link PaymentSchedule} containing one {@link PaymentScheduleItem} per month
     */
    public PaymentSchedule generateSchedule(LoanApplication loanApplication) {
        PaymentSchedule schedule = new PaymentSchedule();
        schedule.setLoanApplication(loanApplication);

        List<PaymentScheduleItem> items = new ArrayList<>();

        LocalDate firstPaymentDate = LocalDate.now();
        int totalMonths = loanApplication.getLoanPeriodMonths();

        // Monthly interest rate.
        BigDecimal monthlyRate = calculateMonthlyInterestRate(
                loanApplication.getInterestMargin(), loanConfigService.getBaseInterest());


        BigDecimal fixedMonthlyPayment = calculateMonthlyPayment(
                loanApplication.getLoanAmount(), monthlyRate, totalMonths)
                .setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);

        // Left over balance.
        BigDecimal balance = loanApplication.getLoanAmount()
                .setScale(RATE_DECIMAL_PLACES, RoundingMode.HALF_UP);

        for (int paymentNumber = 1; paymentNumber <= totalMonths; paymentNumber++) {
            PaymentScheduleItem item = new PaymentScheduleItem();
            item.setPaymentSchedule(schedule);
            item.setPaymentNumber(paymentNumber);
            item.setDueDate(firstPaymentDate.plusMonths(paymentNumber - 1));

            // Interest for the current month.
            BigDecimal interest = balance.multiply(monthlyRate)
                    .setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);

            BigDecimal principal = fixedMonthlyPayment.subtract(interest);
            BigDecimal totalAmount;

            if (paymentNumber == totalMonths) {
                // Final payment: pay off the exact remaining balance to avoid rounding residual
                principal = balance.setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
                totalAmount = principal.add(interest).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
                balance = BigDecimal.ZERO;
            } else {
                totalAmount = fixedMonthlyPayment;
                balance = balance.subtract(principal).setScale(RATE_DECIMAL_PLACES, RoundingMode.HALF_UP);
            }

            item.setTotalAmount(totalAmount);
            item.setRemainingBalance(balance.setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP));

            items.add(item);
        }

        schedule.setItems(items);
        return schedule;
    }

    /**
     * Calculates the fixed monthly annuity payment using the standard formula:
     *
     * @param loanAmount  the total loan principal
     * @param monthlyRate the monthly interest rate (decimal, e.g. 0.005 for 0.5%)
     * @param totalMonths the loan period in months
     * @return the fixed monthly payment amount
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, BigDecimal monthlyRate, int totalMonths) {
        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal growthFactor = onePlusRate.pow(totalMonths, mc);

        BigDecimal numerator = loanAmount
                .multiply(monthlyRate, mc)
                .multiply(growthFactor, mc);

        BigDecimal denominator = growthFactor.subtract(BigDecimal.ONE, mc);

        return numerator.divide(denominator, mc);
    }

    /**
     * Converts an annual percentage rate (margin + base) into a monthly decimal rate.
     *
     * @param interestMargin the bank's margin in percent
     * @param baseInterest   the reference rate in percent
     * @return the monthly interest rate as a decimal, rounded to 10 decimal places
     */
    private BigDecimal calculateMonthlyInterestRate(BigDecimal interestMargin, BigDecimal baseInterest) {
        return interestMargin
                .add(baseInterest)
                .divide(ANNUAL_TO_MONTHLY_DIVISOR, 10, RoundingMode.HALF_UP);
    }
}
