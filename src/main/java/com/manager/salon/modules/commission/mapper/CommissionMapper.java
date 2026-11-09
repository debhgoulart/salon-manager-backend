package com.manager.salon.modules.commission.mapper;

import com.manager.salon.modules.commission.dto.CommissionResponse;
import com.manager.salon.modules.commission.entity.Commission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommissionMapper {

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    CommissionResponse toResponse(Commission commission);
}
