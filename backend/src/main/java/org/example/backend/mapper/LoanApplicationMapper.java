package org.example.backend.mapper;

import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
import org.example.backend.dto.PaymentScheduleItemDTO;
import org.example.backend.dto.RegenerateScheduleRequest;
import org.example.backend.entity.LoanApplication;
import org.example.backend.entity.PaymentScheduleItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loanApplicationStatus", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "paymentSchedule", ignore = true)
    @Mapping(target = "baseInterest", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);

    LoanApplicationResponse toResponse(LoanApplication application, List<PaymentScheduleItem> paymentScheduleItems);

    List<PaymentScheduleItemDTO> toPaymentScheduleItems(List<PaymentScheduleItem> items);

    @BeanMapping(
            ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "loanAmount", source = "loanAmount")
    @Mapping(target = "interestMargin", source = "interestMargin")
    @Mapping(target = "loanPeriodMonths", source = "loanPeriodMonths")
    void updateFromRegenerateRequest(
            RegenerateScheduleRequest request,
            @MappingTarget LoanApplication entity
    );
}
