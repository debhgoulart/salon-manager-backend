package com.manager.salon.modules.appointment.service;

import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.appointment.dto.TimeSlotResponse;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.repository.AppointmentRepository;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.employee.repository.EmployeeRepository;
import com.manager.salon.modules.service.entity.ServiceEntity;
import com.manager.salon.modules.service.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private static final LocalTime SALON_OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime SALON_CLOSE_TIME = LocalTime.of(20, 0);
    private static final int SLOT_INTERVAL_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;

    public AvailabilityService(
            AppointmentRepository appointmentRepository,
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(UUID employeeId, UUID serviceId, LocalDate date, ZoneOffset zoneOffset) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + employeeId));

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + serviceId));

        ZoneOffset offset = zoneOffset != null ? zoneOffset : ZoneOffset.of("-03:00");
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(offset);
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(offset);

        List<Appointment> existingAppointments = appointmentRepository.findActiveByEmployeeAndDay(
                employee.getId(), startOfDay, endOfDay
        );

        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime currentSlotTime = SALON_OPEN_TIME;
        int duration = service.getDurationMinutes();

        while (!currentSlotTime.plusMinutes(duration).isAfter(SALON_CLOSE_TIME)) {
            OffsetDateTime slotStart = date.atTime(currentSlotTime).atOffset(offset);
            OffsetDateTime slotEnd = slotStart.plusMinutes(duration);

            boolean isAvailable = existingAppointments.stream().noneMatch(existing ->
                    slotStart.isBefore(existing.getEndTime()) && slotEnd.isAfter(existing.getStartTime())
            );

            slots.add(new TimeSlotResponse(slotStart, slotEnd, isAvailable));
            currentSlotTime = currentSlotTime.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return slots;
    }
}
