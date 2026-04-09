package org.example.backend.repository;

import org.example.backend.entity.PaymentSchedule;
import org.example.backend.entity.PaymentScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentScheduleItemRepository extends JpaRepository<PaymentScheduleItem, Long> {
    List<PaymentScheduleItem> findByPaymentSchedule(PaymentSchedule paymentSchedule);
}
