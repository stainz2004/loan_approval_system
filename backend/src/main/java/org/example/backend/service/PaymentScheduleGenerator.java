package org.example.backend.service;

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

@Service
public class PaymentScheduleGenerator {

    private static final int CURRENCY_DECIMAL_PLACES = 2;
    private static final int RATE_DECIMAL_PLACES = 10;
    private static final BigDecimal ONE_THOUSAND_TWO_HUNDRED = BigDecimal.valueOf(1200);

    /**
     * Generates a payment schedule for a given loan application.
     *
     * @param loanApplication The loan application for which to generate the payment schedule.
     * @return A PaymentSchedule object containing the payment schedule items.
     */
    public PaymentSchedule generateSchedule(LoanApplication loanApplication) {
        PaymentSchedule schedule = new PaymentSchedule();
        schedule.setLoanApplication(loanApplication);

        List<PaymentScheduleItem> items = new ArrayList<>();

        LocalDate firstPaymentDate = LocalDate.now();
        int totalMonths = loanApplication.getLoanPeriodMonths();

        BigDecimal monthlyRate = calculateMonthlyInterestRate(loanApplication.getInterestMargin(), loanApplication.getBaseInterest());

        BigDecimal fixedMonthlyPayment = calculateMonthlyPayment(loanApplication.getLoanAmount(), monthlyRate, totalMonths).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);

        BigDecimal balance = loanApplication.getLoanAmount().setScale(RATE_DECIMAL_PLACES, RoundingMode.HALF_UP);

        for (int paymentNumber = 1; paymentNumber <= totalMonths; paymentNumber++) {
            PaymentScheduleItem item = new PaymentScheduleItem();
            item.setPaymentSchedule(schedule);
            item.setPaymentNumber(paymentNumber);
            item.setDueDate(firstPaymentDate.plusMonths(paymentNumber - 1));

            BigDecimal interest = balance.multiply(monthlyRate).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
            BigDecimal principal = fixedMonthlyPayment.subtract(interest);
            BigDecimal totalAmount;

            if (paymentNumber == totalMonths) {
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
     * Calculates the monthly payment amount based on the total loan amount and the number of months.
     *
     * @param loanAmount The total amount of the loan.
     * @param monthlyRate The monthly interest rate for the loan.
     * @param totalMonths The loan period in months.
     * @return The calculated monthly payment amount, rounded to 2 decimal places.
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
     * Calculates the monthly interest rate by adding the interest margin and base interest, then dividing by 1200
     * (equivalent to dividing by 100 to convert from percentage, then by 12 to get the monthly rate).
     *
     * @param interestMargin The interest margin for the loan application.
     * @param baseInterest The base interest for the loan application.
     * @return The calculated monthly interest rate, rounded to 10 decimal places.
     */
    private BigDecimal calculateMonthlyInterestRate(BigDecimal interestMargin, BigDecimal baseInterest) {
        return interestMargin
                .add(baseInterest)
                .divide(ONE_THOUSAND_TWO_HUNDRED, 10, RoundingMode.HALF_UP);
    }
}
