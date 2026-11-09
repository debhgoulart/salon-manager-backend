package com.manager.salon.modules.financial.mapper;

import com.manager.salon.modules.financial.dto.FinancialTransactionCreateRequest;
import com.manager.salon.modules.financial.dto.FinancialTransactionResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionUpdateRequest;
import com.manager.salon.modules.financial.entity.FinancialTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FinancialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "monthlyClosingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FinancialTransaction toEntity(FinancialTransactionCreateRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    FinancialTransactionResponse toResponse(FinancialTransaction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "monthlyClosingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(FinancialTransactionUpdateRequest request, @MappingTarget FinancialTransaction entity);
}
