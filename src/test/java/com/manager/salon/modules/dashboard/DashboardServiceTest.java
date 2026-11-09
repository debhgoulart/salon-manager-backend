package com.manager.salon.modules.dashboard;

import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.appointment.repository.AppointmentRepository;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import com.manager.salon.modules.dashboard.dto.DashboardOverviewResponse;
import com.manager.salon.modules.dashboard.dto.TopEmployeeResponse;
import com.manager.salon.modules.dashboard.dto.TopServiceResponse;
import com.manager.salon.modules.dashboard.dto.UpcomingAppointmentResponse;
import com.manager.salon.modules.dashboard.service.DashboardService;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private CommissionRepository commissionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve calcular indicadores do overview financeiro e taxa de cancelamento corretamente")
    void shouldCalculateOverviewCorrectly() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        // Faturamento de serviço: 15.000,00
        when(transactionRepository.sumAmountByTypeAndCategoryAndStatus(
                eq(TransactionType.INCOME), eq("SERVICO"), eq(TransactionStatus.COMPLETED), eq(start), eq(end)
        )).thenReturn(new BigDecimal("15000.00"));

        // Faturamento total: 16.000,00 (1.000 de receita extra)
        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(
                eq(TransactionType.INCOME), eq(TransactionStatus.COMPLETED), eq(start), eq(end)
        )).thenReturn(new BigDecimal("16000.00"));

        // Despesas: 3.500,00
        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(
                eq(TransactionType.EXPENSE), eq(TransactionStatus.COMPLETED), eq(start), eq(end)
        )).thenReturn(new BigDecimal("3500.00"));

        // Comissões: 4.500,00
        when(commissionRepository.sumCommissionsByPeriod(any(), any()))
                .thenReturn(new BigDecimal("4500.00"));

        // Agendamentos: 100 total, 80 concluídos, 15 cancelados, 5 no-show (taxa de cancelamento: 20%)
        when(appointmentRepository.countByStartTimeBetween(any(), any())).thenReturn(100L);
        when(appointmentRepository.countByStatusAndStartTimeBetween(eq(AppointmentStatus.CONCLUIDO), any(), any())).thenReturn(80L);
        when(appointmentRepository.countByStatusAndStartTimeBetween(eq(AppointmentStatus.CANCELADO), any(), any())).thenReturn(15L);
        when(appointmentRepository.countByStatusAndStartTimeBetween(eq(AppointmentStatus.NAO_COMPARECEU), any(), any())).thenReturn(5L);

        DashboardOverviewResponse response = dashboardService.getOverview(start, end);

        assertNotNull(response);
        assertEquals(new BigDecimal("15000.00"), response.grossRevenue());
        assertEquals(new BigDecimal("1000.00"), response.extraRevenue());
        assertEquals(new BigDecimal("3500.00"), response.totalExpenses());
        assertEquals(new BigDecimal("4500.00"), response.totalCommissions());
        // Lucro líquido: 15.000 + 1.000 - 3.500 - 4.500 = 8.000,00
        assertEquals(new BigDecimal("8000.00"), response.netProfit());
        assertEquals(100L, response.totalAppointments());
        assertEquals(80L, response.completedAppointments());
        // Taxa de cancelamento: (15 + 5) / 100 = 20.00%
        assertEquals(new BigDecimal("20.00"), response.cancellationRatePercentage());
    }

    @Test
    @DisplayName("Deve retornar ranking dos top serviços")
    void shouldReturnTopServices() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        List<TopServiceResponse> mockList = List.of(
                new TopServiceResponse(UUID.randomUUID(), "Corte Feminino", 50L, new BigDecimal("6000.00")),
                new TopServiceResponse(UUID.randomUUID(), "Manicure", 40L, new BigDecimal("2000.00"))
        );

        when(appointmentRepository.findTopServices(any(), any(), any())).thenReturn(mockList);

        List<TopServiceResponse> result = dashboardService.getTopServices(start, end, 5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Corte Feminino", result.get(0).serviceName());
    }

    @Test
    @DisplayName("Deve retornar ranking dos top colaboradores")
    void shouldReturnTopEmployees() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        List<TopEmployeeResponse> mockList = List.of(
                new TopEmployeeResponse(UUID.randomUUID(), "Juliana Hair", 60L, new BigDecimal("8500.00"))
        );

        when(appointmentRepository.findTopEmployees(any(), any(), any())).thenReturn(mockList);

        List<TopEmployeeResponse> result = dashboardService.getTopEmployees(start, end, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juliana Hair", result.get(0).employeeName());
    }

    @Test
    @DisplayName("Deve retornar fila de próximos agendamentos")
    void shouldReturnUpcomingAppointments() {
        List<UpcomingAppointmentResponse> mockList = List.of(
                new UpcomingAppointmentResponse(
                        UUID.randomUUID(), "Beatriz", "Juliana", "Escova",
                        OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2),
                        new BigDecimal("80.00"), AppointmentStatus.AGENDADO
                )
        );

        when(appointmentRepository.findUpcomingAppointments(any(), any())).thenReturn(mockList);

        List<UpcomingAppointmentResponse> result = dashboardService.getUpcomingAppointments(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Beatriz", result.get(0).clientName());
    }
}
