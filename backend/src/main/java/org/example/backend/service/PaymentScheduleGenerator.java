package org.example.backend.service;

import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentScheduleGenerator {

    private static final int CURRENCY_DECIMAL_PLACES = 2;

    public PaymentSchedule generateSchedule(LoanApplication loanApplication) {
        PaymentSchedule schedule = new PaymentSchedule();
        schedule.setLoanApplication(loanApplication);

        List<PaymentScheduleItem> items = new ArrayList<>();

        BigDecimal totalAmount = calculateTotalLoanAmount(
                loanApplication.getLoanAmount(),
                loanApplication.getInterestMargin(),
                loanApplication.getBaseInterest()
        );

        int months = loanApplication.getLoanPeriodMonths();
        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, months);
        BigDecimal remainingBalance = totalAmount;

        for (int i = 1; i <= months; i++) {
            PaymentScheduleItem item = new PaymentScheduleItem();
            item.setPaymentSchedule(schedule);
            item.setPaymentNumber(i);
            BigDecimal paymentAmount = (i == months) ? remainingBalance : monthlyPayment;
            item.setTotalAmount(paymentAmount);
            remainingBalance = remainingBalance.subtract(paymentAmount);
            item.setRemainingBalance(remainingBalance.setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP));

            items.add(item);
        }

        schedule.setItems(items);
        return schedule;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal totalLoan, int months) {
        return totalLoan.divide(BigDecimal.valueOf(months), CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalLoanAmount(BigDecimal loanAmount, BigDecimal interestMargin, BigDecimal baseInterest) {
        BigDecimal interestMultiplier = interestMargin.multiply(baseInterest);
        return loanAmount.multiply(interestMultiplier).setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
}
