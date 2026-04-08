package org.example.backend.service;

import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentScheduleGenerator {

    public PaymentSchedule generateSchedule(LoanApplication loanApplication) {
        PaymentSchedule schedule = new PaymentSchedule();
        schedule.setLoanApplication(loanApplication);

        List<PaymentScheduleItem> items = new ArrayList<>();


        BigDecimal totalAmount = loanApplication.getLoanAmount()
        for (int i = 1; i <= loanApplication.getLoanPeriodMonths(); i++) {
            PaymentScheduleItem item = new PaymentScheduleItem();
            item.setPaymentSchedule(schedule);

            item.setPaymentNumber(i);
            items.add(item);
        }

        schedule.setItems(items);
        return schedule;
    }


}
