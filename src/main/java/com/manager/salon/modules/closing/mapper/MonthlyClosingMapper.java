package com.manager.salon.modules.closing.mapper;

import com.manager.salon.modules.closing.dto.MonthlyClosingItemResponse;
import com.manager.salon.modules.closing.dto.MonthlyClosingResponse;
import com.manager.salon.modules.closing.dto.MonthlyClosingSummaryResponse;
import com.manager.salon.modules.closing.entity.MonthlyClosing;
import com.manager.salon.modules.closing.entity.MonthlyClosingItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonthlyClosingMapper {

    @Mapping(target = "items", source = "items")
    MonthlyClosingResponse toResponse(MonthlyClosing entity);

    MonthlyClosingSummaryResponse toSummaryResponse(MonthlyClosing entity);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    MonthlyClosingItemResponse toItemResponse(MonthlyClosingItem item);
}
