package com.manager.salon.modules.appointment;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ScheduleConflictException;
import com.manager.salon.modules.appointment.dto.AppointmentCreateRequest;
import com.manager.salon.modules.appointment.dto.AppointmentResponse;
import com.manager.salon.modules.appointment.dto.UpdateAppointmentStatusRequest;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.appointment.mapper.AppointmentMapper;
import com.manager.salon.modules.appointment.repository.AppointmentRepository;
import com.manager.salon.modules.appointment.service.AppointmentService;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.client.repository.ClientRepository;
import com.manager.salon.modules.commission.service.CommissionCalculatorService;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.employee.repository.EmployeeRepository;
import com.manager.salon.modules.financial.service.FinancialTransactionService;
import com.manager.salon.modules.service.entity.ServiceEntity;
import com.manager.salon.modules.service.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private FinancialTransactionService financialTransactionService;

    @Mock
    private CommissionCalculatorService commissionCalculatorService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Client client;
    private Employee employee;
    private ServiceEntity serviceEntity;
    private Appointment appointment;
    private UUID clientId;
    private UUID employeeId;
    private UUID serviceId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        client = new Client("Cliente Teste", "11999998888", "cliente@email.com", LocalDate.now(), null, true);
        client.setId(clientId);

        employee = new Employee(null, "Profissional", "11122233344", "11888887777", "pro@salon.com", "Cabeleireiro", LocalDate.now(), new BigDecimal("40.00"), true);
        employee.setId(employeeId);

        serviceEntity = new ServiceEntity("Corte", "Desc", new BigDecimal("100.00"), 60, null, true);
        serviceEntity.setId(serviceId);

        OffsetDateTime start = LocalDate.now().plusDays(1).atTime(10, 0).atOffset(ZoneOffset.of("-03:00"));
        OffsetDateTime end = start.plusMinutes(60);

        appointment = new Appointment(client, employee, serviceEntity, start, end, new BigDecimal("100.00"), AppointmentStatus.AGENDADO, "Nota");
        appointment.setId(appointmentId);
    }

    @Test
    @DisplayName("Deve criar agendamento com cálculo correto de horário de término")
    void shouldCreateAppointmentSuccessfully() {
        OffsetDateTime startTime = LocalDate.now().plusDays(1).atTime(14, 0).atOffset(ZoneOffset.of("-03:00"));
        AppointmentCreateRequest request = new AppointmentCreateRequest(clientId, employeeId, serviceId, startTime, "Sem amônia");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceEntity));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(new AppointmentResponse(
                appointmentId, clientId, "Cliente Teste", "11999998888",
                employeeId, "Profissional", serviceId, "Corte", 60,
                startTime, startTime.plusMinutes(60), new BigDecimal("100.00"),
                AppointmentStatus.AGENDADO, "Sem amônia", 0L, null, null
        ));

        AppointmentResponse response = appointmentService.create(request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.AGENDADO, response.status());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve lançar ScheduleConflictException se houver sobreposição na agenda")
    void shouldThrowExceptionWhenScheduleConflicts() {
        OffsetDateTime startTime = LocalDate.now().plusDays(1).atTime(14, 0).atOffset(ZoneOffset.of("-03:00"));
        AppointmentCreateRequest request = new AppointmentCreateRequest(clientId, employeeId, serviceId, startTime, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceEntity));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any())).thenReturn(List.of(appointment));

        assertThrows(ScheduleConflictException.class, () -> appointmentService.create(request));
    }

    @Test
    @DisplayName("Deve lançar BusinessException se o colaborador estiver inativo")
    void shouldThrowExceptionWhenEmployeeIsInactive() {
        employee.setActive(false);
        OffsetDateTime startTime = LocalDate.now().plusDays(1).atTime(14, 0).atOffset(ZoneOffset.of("-03:00"));
        AppointmentCreateRequest request = new AppointmentCreateRequest(clientId, employeeId, serviceId, startTime, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class, () -> appointmentService.create(request));
    }

    @Test
    @DisplayName("Deve lançar BusinessException se o horário for fora do expediente do salão")
    void shouldThrowExceptionWhenOutsideOperatingHours() {
        // 06:00 AM (Salão abre às 08:00)
        OffsetDateTime earlyTime = LocalDate.now().plusDays(1).atTime(6, 0).atOffset(ZoneOffset.of("-03:00"));
        AppointmentCreateRequest request = new AppointmentCreateRequest(clientId, employeeId, serviceId, earlyTime, null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceEntity));

        assertThrows(BusinessException.class, () -> appointmentService.create(request));
    }

    @Test
    @DisplayName("Deve atualizar status com transição válida")
    void shouldUpdateStatusSuccessfully() {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONFIRMADO, "Confirmou por WhatsApp");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(new AppointmentResponse(
                appointmentId, clientId, "Cliente Teste", "11999998888",
                employeeId, "Profissional", serviceId, "Corte", 60,
                appointment.getStartTime(), appointment.getEndTime(), new BigDecimal("100.00"),
                AppointmentStatus.CONFIRMADO, null, 0L, null, null
        ));

        AppointmentResponse response = appointmentService.updateStatus(appointmentId, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CONFIRMADO, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Deve rejeitar transição inválida de status")
    void shouldRejectInvalidStatusTransition() {
        // AGENDADO direto para CONCLUIDO não é permitido
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest(AppointmentStatus.CONCLUIDO, null);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class, () -> appointmentService.updateStatus(appointmentId, request));
    }

    @Test
    @DisplayName("Deve cancelar agendamento com sucesso")
    void shouldCancelAppointmentSuccessfully() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.cancel(appointmentId, "Cliente solicitou cancelamento");

        assertEquals(AppointmentStatus.CANCELADO, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }
}
