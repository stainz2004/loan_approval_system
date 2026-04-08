package org.example.backend.mapper;

import org.example.backend.dto.LoanApplicationRequest;
import org.example.backend.entity.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    @Mapping(target = "status", constant = "STARTED")
    LoanApplication toEntity(LoanApplicationRequest request);
}
