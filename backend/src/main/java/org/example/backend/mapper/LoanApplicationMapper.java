package org.example.backend.mapper;

import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.dto.LoanApplicationResponse;
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

    @Mapping(target = "loanApplicationStatus", constant = "STARTED")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "paymentSchedule", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);

    LoanApplicationResponse toResponse(LoanApplication application, List<PaymentScheduleItem> paymentScheduleItems);

    @BeanMapping(
            ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "loanAmount", source = "loanAmount")
    @Mapping(target = "interestMargin", source = "interestMargin")
    @Mapping(target = "baseInterest", source = "baseInterest")
    @Mapping(target = "loanPeriodMonths", source = "loanPeriodMonths")
    void updateFromRegenerateRequest(
            RegenerateScheduleRequest request,
            @MappingTarget LoanApplication entity
    );
}
