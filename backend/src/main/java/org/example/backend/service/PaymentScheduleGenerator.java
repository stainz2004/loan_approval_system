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
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

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
        BigDecimal remainingBalance = loanApplication.getLoanAmount();
        int months = loanApplication.getLoanPeriodMonths();

        BigDecimal monthlyRate = calculateMonthlyInterestRate(
                loanApplication.getInterestMargin(),
                loanApplication.getBaseInterest()
        );

        BigDecimal monthlyInstallment = calculateMonthlyPayment(
                loanApplication.getLoanAmount(),
                monthlyRate,
                months
        );

        for (int paymentNumber = 1; paymentNumber <= months; paymentNumber++) {
            PaymentScheduleItem item = new PaymentScheduleItem();
            item.setPaymentSchedule(schedule);
            item.setPaymentNumber(paymentNumber);
            item.setDueDate(firstPaymentDate.plusMonths(paymentNumber - 1));

            BigDecimal interestPart = calculateInterestPart(remainingBalance, monthlyRate);
            BigDecimal principalPart = monthlyInstallment.subtract(interestPart).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);

            if (paymentNumber == months) {
                principalPart = remainingBalance;
                monthlyInstallment = principalPart.add(interestPart).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
            }

            remainingBalance = remainingBalance.subtract(principalPart).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);

            item.setTotalAmount(monthlyInstallment);
            item.setRemainingBalance(remainingBalance);

            items.add(item);
        }

        schedule.setItems(items);
        return schedule;
    }

    /**
     * Calculates the monthly payment amount based on the total loan amount and the number of months.
     *
     * @param months The loan period in months.
     * @return The calculated monthly payment amount, rounded to 2 decimal places.
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, BigDecimal monthlyInterestRate, int months) {
        MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);

        BigDecimal onePlusInterest = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal onePlusInterestToMonths = onePlusInterest.pow(months, mathContext);

        BigDecimal topPart = loanAmount
                .multiply(monthlyInterestRate)
                .multiply(onePlusInterestToMonths, mathContext);

        BigDecimal bottomPart = onePlusInterestToMonths.subtract(BigDecimal.ONE, mathContext);

        return topPart
                .divide(bottomPart, mathContext)
                .setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    /**
     *
     * @param interestMargin
     * @param baseInterest
     * @return
     */
    private BigDecimal calculateMonthlyInterestRate(BigDecimal interestMargin, BigDecimal baseInterest) {
        return interestMargin
                .add(baseInterest)
                .divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);
    }

    /**
     *
     * @param remainingBalance
     * @param monthlyRate
     * @return
     */
    private BigDecimal calculateInterestPart(BigDecimal remainingBalance, BigDecimal monthlyRate) {
        return remainingBalance.multiply(monthlyRate).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
}
