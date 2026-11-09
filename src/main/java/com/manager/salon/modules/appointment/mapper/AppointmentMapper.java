package com.manager.salon.modules.appointment.mapper;

import com.manager.salon.modules.appointment.dto.AppointmentResponse;
import com.manager.salon.modules.appointment.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "serviceDurationMinutes", source = "service.durationMinutes")
    AppointmentResponse toResponse(Appointment appointment);
}
