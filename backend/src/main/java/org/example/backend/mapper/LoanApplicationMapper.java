package org.example.backend.mapper;

import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.PaymentScheduleItemDTO;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentScheduleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    @Mapping(target = "loanApplicationStatus", constant = "STARTED")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);

    LoanApplicationResponse toResponse(LoanApplication application, java.util.List<PaymentScheduleItem> paymentScheduleItems);

    PaymentScheduleItemDTO toPaymentScheduleItemDTO(PaymentScheduleItem item);
}
