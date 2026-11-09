package com.manager.salon.modules.commission;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.commission.dto.CommissionResponse;
import com.manager.salon.modules.commission.entity.Commission;
import com.manager.salon.modules.commission.entity.CommissionStatus;
import com.manager.salon.modules.commission.mapper.CommissionMapper;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import com.manager.salon.modules.commission.service.CommissionCalculatorService;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.service.entity.ServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommissionCalculatorServiceTest {

    @Mock
    private CommissionRepository commissionRepository;

    @Mock
    private CommissionMapper commissionMapper;

    @InjectMocks
    private CommissionCalculatorService commissionService;

    private Employee employee;
    private ServiceEntity serviceWithCustomRate;
    private ServiceEntity serviceWithoutCustomRate;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "Maria Cabeleireira", "12345678900", "11999998888", "maria@salon.com", "Cabeleireira", LocalDate.now(), new BigDecimal("40.00"), true);
        employee.setId(UUID.randomUUID());

        // Serviço com taxa customizada de 50%
        serviceWithCustomRate = new ServiceEntity("Coloração Especial", "Desc", new BigDecimal("200.00"), 90, new BigDecimal("50.00"), true);
        serviceWithCustomRate.setId(UUID.randomUUID());

        // Serviço sem taxa customizada (deve usar 40% do funcionário)
        serviceWithoutCustomRate = new ServiceEntity("Corte Simples", "Desc", new BigDecimal("100.00"), 45, null, true);
        serviceWithoutCustomRate.setId(UUID.randomUUID());

        Client client = new Client("Cliente", "11888887777", "cliente@email.com", LocalDate.now(), null, true);
        client.setId(UUID.randomUUID());

        appointment = new Appointment(client, employee, serviceWithCustomRate, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(90), new BigDecimal("200.00"), AppointmentStatus.CONCLUIDO, null);
        appointment.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Precedência: Deve aplicar taxa customizada do serviço (50%) com prioridade sobre a taxa do colaborador (40%)")
    void shouldUseServiceCustomCommissionRateWhenPresent() {
        when(commissionRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.empty());
        when(commissionRepository.save(any(Commission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Commission commission = commissionService.calculateAndRecordCommission(appointment);

        assertNotNull(commission);
        // 50% de 200 = 100 de comissão, 100 para o salão
        assertEquals(new BigDecimal("50.00"), commission.getAppliedRate());
        assertEquals(new BigDecimal("200.00"), commission.getServiceAmount());
        assertEquals(new BigDecimal("100.00"), commission.getCommissionAmount());
        assertEquals(new BigDecimal("100.00"), commission.getSalonAmount());
        assertEquals(CommissionStatus.PENDING, commission.getStatus());

        ArgumentCaptor<Commission> captor = ArgumentCaptor.forClass(Commission.class);
        verify(commissionRepository).save(captor.capture());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getCommissionAmount());
    }

    @Test
    @DisplayName("Precedência: Deve usar a taxa padrão do funcionário quando o serviço não possui taxa específica")
    void shouldUseEmployeeDefaultRateWhenServiceHasNoCustomRate() {
        appointment.setService(serviceWithoutCustomRate);
        appointment.setEstimatedPrice(new BigDecimal("100.00"));

        when(commissionRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.empty());
        when(commissionRepository.save(any(Commission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Commission commission = commissionService.calculateAndRecordCommission(appointment);

        assertNotNull(commission);
        // 40% de 100 = 40 de comissão, 60 para o salão
        assertEquals(new BigDecimal("40.00"), commission.getAppliedRate());
        assertEquals(new BigDecimal("100.00"), commission.getServiceAmount());
        assertEquals(new BigDecimal("40.00"), commission.getCommissionAmount());
        assertEquals(new BigDecimal("60.00"), commission.getSalonAmount());
    }

    @Test
    @DisplayName("Precisão e Arredondamento Bancário (HALF_EVEN)")
    void shouldCalculateAccurateCommissionWithRoundingHalfEven() {
        // Exemplo: Serviço de R$ 125,55 com taxa de 33,33%
        // 125.55 * 33.33 / 100 = 41.845815 -> arredonda para 41.85
        employee.setDefaultCommissionRate(new BigDecimal("33.33"));
        appointment.setService(serviceWithoutCustomRate);
        appointment.setEstimatedPrice(new BigDecimal("125.55"));

        when(commissionRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.empty());
        when(commissionRepository.save(any(Commission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Commission commission = commissionService.calculateAndRecordCommission(appointment);

        assertNotNull(commission);
        assertEquals(new BigDecimal("41.85"), commission.getCommissionAmount());
        assertEquals(new BigDecimal("83.70"), commission.getSalonAmount());
    }

    @Test
    @DisplayName("Deve marcar comissão como paga")
    void shouldMarkCommissionAsPaid() {
        UUID commissionId = UUID.randomUUID();
        Commission commission = new Commission(appointment, employee, null, new BigDecimal("40.00"), new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"), CommissionStatus.PENDING);
        commission.setId(commissionId);

        when(commissionRepository.findById(commissionId)).thenReturn(Optional.of(commission));
        when(commissionRepository.save(commission)).thenReturn(commission);
        when(commissionMapper.toResponse(commission)).thenReturn(new CommissionResponse(
                commissionId, appointment.getId(), employee.getId(), employee.getName(), null,
                new BigDecimal("40.00"), new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"),
                CommissionStatus.PAID, null, null
        ));

        CommissionResponse response = commissionService.markAsPaid(commissionId);

        assertNotNull(response);
        assertEquals(CommissionStatus.PAID, commission.getStatus());
        verify(commissionRepository).save(commission);
    }

    @Test
    @DisplayName("Deve rejeitar pagamento de comissão cancelada")
    void shouldThrowExceptionWhenPayingCancelledCommission() {
        UUID commissionId = UUID.randomUUID();
        Commission commission = new Commission(appointment, employee, null, new BigDecimal("40.00"), new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"), CommissionStatus.CANCELLED);
        commission.setId(commissionId);

        when(commissionRepository.findById(commissionId)).thenReturn(Optional.of(commission));

        assertThrows(BusinessException.class, () -> commissionService.markAsPaid(commissionId));
    }
}
