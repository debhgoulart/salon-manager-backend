package com.manager.salon.modules.financial;

import com.manager.salon.exception.ClosedPeriodException;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import com.manager.salon.modules.closing.repository.MonthlyClosingRepository;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.financial.dto.FinancialSummaryResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionCreateRequest;
import com.manager.salon.modules.financial.dto.FinancialTransactionResponse;
import com.manager.salon.modules.financial.entity.FinancialTransaction;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.mapper.FinancialMapper;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
import com.manager.salon.modules.financial.service.FinancialTransactionService;
import com.manager.salon.modules.service.entity.ServiceEntity;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialTransactionServiceTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private FinancialMapper financialMapper;

    @Mock
    private MonthlyClosingRepository monthlyClosingRepository;

    @InjectMocks
    private FinancialTransactionService financialService;

    private FinancialTransaction transaction;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        transaction = new FinancialTransaction(
                null,
                null,
                "Conta de Luz",
                "CONTAS",
                new BigDecimal("250.00"),
                LocalDate.of(2026, 11, 10),
                TransactionType.EXPENSE,
                TransactionStatus.COMPLETED,
                "Pago via PIX"
        );
        transaction.setId(transactionId);
    }

    @Test
    @DisplayName("Deve criar lançamento financeiro quando o período não estiver fechado")
    void shouldCreateTransactionWhenPeriodIsOpen() {
        FinancialTransactionCreateRequest request = new FinancialTransactionCreateRequest(
                "Conta de Luz", "CONTAS", new BigDecimal("250.00"),
                LocalDate.of(2026, 11, 10), TransactionType.EXPENSE,
                TransactionStatus.COMPLETED, "Pago via PIX"
        );

        when(monthlyClosingRepository.existsByReferenceYearAndReferenceMonthAndStatus(2026, 11, MonthlyClosingStatus.CLOSED))
                .thenReturn(false);
        when(financialMapper.toEntity(request)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(financialMapper.toResponse(transaction)).thenReturn(new FinancialTransactionResponse(
                transactionId, null, null, "Conta de Luz", "CONTAS",
                new BigDecimal("250.00"), LocalDate.of(2026, 11, 10),
                TransactionType.EXPENSE, TransactionStatus.COMPLETED, "Pago via PIX", null, null
        ));

        FinancialTransactionResponse response = financialService.create(request);

        assertNotNull(response);
        assertEquals("Conta de Luz", response.description());
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Deve bloquear criação de lançamento em período fechado (ClosedPeriodException)")
    void shouldBlockCreationWhenPeriodIsClosed() {
        FinancialTransactionCreateRequest request = new FinancialTransactionCreateRequest(
                "Despesa Retroativa", "OUTROS", new BigDecimal("500.00"),
                LocalDate.of(2026, 10, 15), TransactionType.EXPENSE,
                TransactionStatus.COMPLETED, null
        );

        // Mês 10/2026 fechado
        when(monthlyClosingRepository.existsByReferenceYearAndReferenceMonthAndStatus(2026, 10, MonthlyClosingStatus.CLOSED))
                .thenReturn(true);

        assertThrows(ClosedPeriodException.class, () -> financialService.create(request));
    }

    @Test
    @DisplayName("Deve registrar receita automaticamente ao concluir agendamento")
    void shouldRegisterIncomeFromCompletedAppointment() {
        Client client = new Client("Cliente", "11999998888", null, LocalDate.now(), null, true);
        Employee employee = new Employee(null, "Profissional", "123", "phone", "email", "Cargo", LocalDate.now(), new BigDecimal("40"), true);
        ServiceEntity service = new ServiceEntity("Corte", null, new BigDecimal("150.00"), 45, null, true);

        Appointment appointment = new Appointment(
                client, employee, service,
                OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(45),
                new BigDecimal("150.00"), AppointmentStatus.CONCLUIDO, null
        );
        appointment.setId(UUID.randomUUID());

        LocalDate appointmentDate = appointment.getStartTime().toLocalDate();

        when(monthlyClosingRepository.existsByReferenceYearAndReferenceMonthAndStatus(
                appointmentDate.getYear(), appointmentDate.getMonthValue(), MonthlyClosingStatus.CLOSED
        )).thenReturn(false);
        when(transactionRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialTransaction tx = financialService.registerIncomeFromAppointment(appointment);

        assertNotNull(tx);
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(TransactionStatus.COMPLETED, tx.getStatus());
        assertEquals(new BigDecimal("150.00"), tx.getAmount());
        assertEquals("SERVICO", tx.getCategory());
        verify(transactionRepository).save(any(FinancialTransaction.class));
    }

    @Test
    @DisplayName("Deve consolidar resumo financeiro corretamente")
    void shouldCalculateFinancialSummaryCorrectly() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.INCOME, TransactionStatus.COMPLETED, start, end))
                .thenReturn(new BigDecimal("10000.00"));
        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.EXPENSE, TransactionStatus.COMPLETED, start, end))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.INCOME, TransactionStatus.PENDING, start, end))
                .thenReturn(new BigDecimal("1500.00"));
        when(transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.EXPENSE, TransactionStatus.PENDING, start, end))
                .thenReturn(new BigDecimal("500.00"));

        FinancialSummaryResponse summary = financialService.getSummary(start, end);

        assertNotNull(summary);
        assertEquals(new BigDecimal("10000.00"), summary.totalIncomeCompleted());
        assertEquals(new BigDecimal("3000.00"), summary.totalExpenseCompleted());
        // Resultado líquido operacional: 10000 - 3000 = 7000
        assertEquals(new BigDecimal("7000.00"), summary.netOperationalResult());
        assertEquals(new BigDecimal("1500.00"), summary.pendingIncome());
        assertEquals(new BigDecimal("500.00"), summary.pendingExpense());
    }
}
