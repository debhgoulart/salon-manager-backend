package com.manager.salon.modules.closing;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ClosedPeriodException;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.closing.dto.MonthlyClosingCreateRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingReopenRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingResponse;
import com.manager.salon.modules.closing.entity.MonthlyClosing;
import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import com.manager.salon.modules.closing.mapper.MonthlyClosingMapper;
import com.manager.salon.modules.closing.repository.MonthlyClosingRepository;
import com.manager.salon.modules.closing.service.MonthlyClosingService;
import com.manager.salon.modules.commission.entity.Commission;
import com.manager.salon.modules.commission.entity.CommissionStatus;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.financial.entity.FinancialTransaction;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
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
class MonthlyClosingServiceTest {

    @Mock
    private MonthlyClosingRepository monthlyClosingRepository;

    @Mock
    private MonthlyClosingMapper monthlyClosingMapper;

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private CommissionRepository commissionRepository;

    @InjectMocks
    private MonthlyClosingService closingService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "Profissional", "123", "phone", "email", "Cabeleireiro", LocalDate.now(), new BigDecimal("40.00"), true);
        employee.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve consolidar fechamento mensal com faturamento, despesas e comissões")
    void shouldCloseMonthSuccessfully() {
        MonthlyClosingCreateRequest request = new MonthlyClosingCreateRequest(2026, 10);

        when(monthlyClosingRepository.findByReferenceYearAndReferenceMonth(2026, 10))
                .thenReturn(Optional.empty());

        // Receita de serviço: 10.000,00
        FinancialTransaction incomeTx = new FinancialTransaction(
                null, null, "Serviços", "SERVICO", new BigDecimal("10000.00"),
                LocalDate.of(2026, 10, 15), TransactionType.INCOME, TransactionStatus.COMPLETED, null
        );
        incomeTx.setId(UUID.randomUUID());

        // Despesas: 2.000,00
        FinancialTransaction expenseTx = new FinancialTransaction(
                null, null, "Aluguel", "ALUGUEL", new BigDecimal("2000.00"),
                LocalDate.of(2026, 10, 10), TransactionType.EXPENSE, TransactionStatus.COMPLETED, null
        );
        expenseTx.setId(UUID.randomUUID());

        when(transactionRepository.findCompletedUnclosedForPeriod(any(), any()))
                .thenReturn(List.of(incomeTx, expenseTx));

        // Comissão: 3.000,00
        Commission commission = new Commission(
                new Appointment(), employee, null, new BigDecimal("30.00"),
                new BigDecimal("10000.00"), new BigDecimal("3000.00"), new BigDecimal("7000.00"), CommissionStatus.PENDING
        );
        commission.setId(UUID.randomUUID());

        when(commissionRepository.findPendingForClosing(any(), any()))
                .thenReturn(List.of(commission));

        when(monthlyClosingRepository.save(any(MonthlyClosing.class)))
                .thenAnswer(invocation -> {
                    MonthlyClosing mc = invocation.getArgument(0);
                    mc.setId(UUID.randomUUID());
                    return mc;
                });

        when(monthlyClosingMapper.toResponse(any(MonthlyClosing.class)))
                .thenAnswer(invocation -> {
                    MonthlyClosing mc = invocation.getArgument(0);
                    return new MonthlyClosingResponse(
                            mc.getId(), mc.getReferenceYear(), mc.getReferenceMonth(),
                            mc.getGrossRevenue(), mc.getExtraRevenue(), mc.getTotalExpenses(),
                            mc.getTotalCommissions(), mc.getNetResult(), mc.getStatus(),
                            mc.getClosedAt(), mc.getClosedBy(), mc.getAuditNotes(), Collections.emptyList()
                    );
                });

        MonthlyClosingResponse response = closingService.closeMonth(request, "admin@salon.com");

        assertNotNull(response);
        assertEquals(new BigDecimal("10000.00"), response.grossRevenue());
        assertEquals(new BigDecimal("2000.00"), response.totalExpenses());
        assertEquals(new BigDecimal("3000.00"), response.totalCommissions());
        // Resultado líquido restante: 10.000 - 2.000 - 3.000 = 5.000,00
        assertEquals(new BigDecimal("5000.00"), response.netResult());
        assertEquals(MonthlyClosingStatus.CLOSED, response.status());

        verify(monthlyClosingRepository).save(any(MonthlyClosing.class));
    }

    @Test
    @DisplayName("Deve rejeitar fechamento duplicado se o período já estiver fechado (ClosedPeriodException)")
    void shouldRejectDuplicateClosingWhenAlreadyClosed() {
        MonthlyClosingCreateRequest request = new MonthlyClosingCreateRequest(2026, 10);
        MonthlyClosing existingClosed = new MonthlyClosing();
        existingClosed.setStatus(MonthlyClosingStatus.CLOSED);

        when(monthlyClosingRepository.findByReferenceYearAndReferenceMonth(2026, 10))
                .thenReturn(Optional.of(existingClosed));

        assertThrows(ClosedPeriodException.class, () -> closingService.closeMonth(request, "admin@salon.com"));
    }

    @Test
    @DisplayName("Deve reabrir fechamento mensal com notas de auditoria obrigatórias")
    void shouldReopenClosingWithAuditNotes() {
        UUID closingId = UUID.randomUUID();
        MonthlyClosing closed = new MonthlyClosing();
        closed.setId(closingId);
        closed.setStatus(MonthlyClosingStatus.CLOSED);

        MonthlyClosingReopenRequest request = new MonthlyClosingReopenRequest("Ajuste emergencial de despesa não lançada");

        when(monthlyClosingRepository.findById(closingId)).thenReturn(Optional.of(closed));
        when(monthlyClosingRepository.save(closed)).thenReturn(closed);
        when(monthlyClosingMapper.toResponse(closed)).thenReturn(new MonthlyClosingResponse(
                closingId, 2026, 10, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, MonthlyClosingStatus.REOPENED, OffsetDateTime.now(), "ADMIN", "Notas", Collections.emptyList()
        ));

        MonthlyClosingResponse response = closingService.reopen(closingId, request, "gerente@salon.com");

        assertNotNull(response);
        assertEquals(MonthlyClosingStatus.REOPENED, closed.getStatus());
        verify(monthlyClosingRepository).save(closed);
    }

    @Test
    @DisplayName("Deve rejeitar reabertura se o período já estiver em status REOPENED")
    void shouldRejectReopeningWhenAlreadyReopened() {
        UUID closingId = UUID.randomUUID();
        MonthlyClosing alreadyReopened = new MonthlyClosing();
        alreadyReopened.setId(closingId);
        alreadyReopened.setStatus(MonthlyClosingStatus.REOPENED);

        MonthlyClosingReopenRequest request = new MonthlyClosingReopenRequest("Tentando reabrir de novo");

        when(monthlyClosingRepository.findById(closingId)).thenReturn(Optional.of(alreadyReopened));

        assertThrows(BusinessException.class, () -> closingService.reopen(closingId, request, "admin@salon.com"));
    }
}
