package com.manager.salon.modules.appointment.service;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.exception.ScheduleConflictException;
import com.manager.salon.modules.appointment.dto.AppointmentCreateRequest;
import com.manager.salon.modules.appointment.dto.AppointmentResponse;
import com.manager.salon.modules.appointment.dto.AppointmentUpdateRequest;
import com.manager.salon.modules.appointment.dto.UpdateAppointmentStatusRequest;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.appointment.mapper.AppointmentMapper;
import com.manager.salon.modules.appointment.repository.AppointmentRepository;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.client.repository.ClientRepository;
import com.manager.salon.modules.commission.service.CommissionCalculatorService;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.employee.repository.EmployeeRepository;
import com.manager.salon.modules.financial.service.FinancialTransactionService;
import com.manager.salon.modules.service.entity.ServiceEntity;
import com.manager.salon.modules.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final LocalTime SALON_OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime SALON_CLOSE_TIME = LocalTime.of(20, 0);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final FinancialTransactionService financialTransactionService;
    private final CommissionCalculatorService commissionCalculatorService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentMapper appointmentMapper,
            ClientRepository clientRepository,
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository,
            FinancialTransactionService financialTransactionService,
            CommissionCalculatorService commissionCalculatorService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
        this.financialTransactionService = financialTransactionService;
        this.commissionCalculatorService = commissionCalculatorService;
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + request.clientId()));
        if (!client.isActive()) {
            throw new BusinessException("Não é permitido criar agendamentos para clientes inativos.");
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + request.employeeId()));
        if (!employee.isActive()) {
            throw new BusinessException("Não é permitido criar agendamentos para colaboradores inativos.");
        }

        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + request.serviceId()));
        if (!service.isActive()) {
            throw new BusinessException("Não é permitido agendar serviços desativados.");
        }

        OffsetDateTime startTime = request.startTime();
        OffsetDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        validateSalonOperatingHours(startTime, endTime);
        validateScheduleConflict(employee.getId(), startTime, endTime, null);

        Appointment appointment = new Appointment(
                client,
                employee,
                service,
                startTime,
                endTime,
                service.getPrice(),
                AppointmentStatus.AGENDADO,
                request.notes()
        );

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com o ID: " + id));
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(
            UUID employeeId,
            UUID clientId,
            AppointmentStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    ) {
        return appointmentRepository.findAllWithFilters(employeeId, clientId, status, startDate, endDate, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Transactional
    public AppointmentResponse update(UUID id, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com o ID: " + id));

        if (isTerminalStatus(appointment.getStatus())) {
            throw new BusinessException("Não é permitido alterar agendamentos com status: " + appointment.getStatus());
        }

        ServiceEntity service = appointment.getService();
        if (request.serviceId() != null && !request.serviceId().equals(service.getId())) {
            service = serviceRepository.findById(request.serviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + request.serviceId()));
            if (!service.isActive()) {
                throw new BusinessException("Não é permitido associar um serviço inativo.");
            }
            appointment.setService(service);
            appointment.setEstimatedPrice(service.getPrice());
        }

        OffsetDateTime startTime = request.startTime();
        OffsetDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        validateSalonOperatingHours(startTime, endTime);
        validateScheduleConflict(appointment.getEmployee().getId(), startTime, endTime, appointment.getId());

        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        if (request.notes() != null) {
            appointment.setNotes(request.notes());
        }

        Appointment updated = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(updated);
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID id, UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com o ID: " + id));

        AppointmentStatus currentStatus = appointment.getStatus();
        AppointmentStatus newStatus = request.status();

        validateStatusTransition(currentStatus, newStatus);

        appointment.setStatus(newStatus);
        if (request.notes() != null && !request.notes().isBlank()) {
            String updatedNotes = (appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                    "[" + newStatus + "] " + request.notes();
            appointment.setNotes(updatedNotes);
        }

        Appointment updated = appointmentRepository.save(appointment);

        // Gatilhos Financeiros e de Comissões
        if (newStatus == AppointmentStatus.CONCLUIDO) {
            financialTransactionService.registerIncomeFromAppointment(updated);
            commissionCalculatorService.calculateAndRecordCommission(updated);
        } else if (newStatus == AppointmentStatus.CANCELADO || newStatus == AppointmentStatus.NAO_COMPARECEU) {
            financialTransactionService.cancelTransactionByAppointment(updated.getId());
            commissionCalculatorService.cancelCommissionByAppointment(updated.getId());
        }

        return appointmentMapper.toResponse(updated);
    }

    @Transactional
    public void cancel(UUID id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com o ID: " + id));

        if (isTerminalStatus(appointment.getStatus())) {
            throw new BusinessException("Agendamento não pode ser cancelado pois seu status atual é: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELADO);
        if (reason != null && !reason.isBlank()) {
            String updatedNotes = (appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                    "[CANCELAMENTO] " + reason;
            appointment.setNotes(updatedNotes);
        }
        appointmentRepository.save(appointment);
        financialTransactionService.cancelTransactionByAppointment(appointment.getId());
        commissionCalculatorService.cancelCommissionByAppointment(appointment.getId());
    }

    private void validateScheduleConflict(UUID employeeId, OffsetDateTime startTime, OffsetDateTime endTime, UUID excludeId) {
        List<Appointment> conflicts = appointmentRepository.findOverlappingAppointments(employeeId, startTime, endTime, excludeId);
        if (!conflicts.isEmpty()) {
            Appointment conflict = conflicts.get(0);
            throw new ScheduleConflictException(
                    "O colaborador já possui um agendamento conflitante entre " +
                    conflict.getStartTime() + " e " + conflict.getEndTime() + "."
            );
        }
    }

    private void validateSalonOperatingHours(OffsetDateTime startTime, OffsetDateTime endTime) {
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();

        if (start.isBefore(SALON_OPEN_TIME) || end.isAfter(SALON_CLOSE_TIME) || end.isBefore(start)) {
            throw new BusinessException(
                    "O atendimento (" + start + " às " + end + ") extrapola o horário de funcionamento do salão (" +
                    SALON_OPEN_TIME + " às " + SALON_CLOSE_TIME + ")."
            );
        }
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus target) {
        if (current == target) {
            return;
        }

        if (isTerminalStatus(current)) {
            throw new BusinessException("Não é possível alterar o status de um agendamento finalizado em: " + current);
        }

        boolean valid = switch (current) {
            case AGENDADO -> target == AppointmentStatus.CONFIRMADO || target == AppointmentStatus.CANCELADO || target == AppointmentStatus.NAO_COMPARECEU;
            case CONFIRMADO -> target == AppointmentStatus.EM_ATENDIMENTO || target == AppointmentStatus.CANCELADO || target == AppointmentStatus.NAO_COMPARECEU;
            case EM_ATENDIMENTO -> target == AppointmentStatus.CONCLUIDO || target == AppointmentStatus.CANCELADO;
            default -> false;
        };

        if (!valid) {
            throw new BusinessException("Transição de status inválida de " + current + " para " + target + ".");
        }
    }

    private boolean isTerminalStatus(AppointmentStatus status) {
        return status == AppointmentStatus.CONCLUIDO || status == AppointmentStatus.CANCELADO || status == AppointmentStatus.NAO_COMPARECEU;
    }
}
