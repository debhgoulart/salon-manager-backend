package com.manager.salon.modules.financial.service;

import com.manager.salon.exception.ClosedPeriodException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import com.manager.salon.modules.closing.repository.MonthlyClosingRepository;
import com.manager.salon.modules.financial.dto.FinancialSummaryResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionCreateRequest;
import com.manager.salon.modules.financial.dto.FinancialTransactionResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionUpdateRequest;
import com.manager.salon.modules.financial.entity.FinancialTransaction;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.mapper.FinancialMapper;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinancialMapper financialMapper;
    private final MonthlyClosingRepository monthlyClosingRepository;

    public FinancialTransactionService(
            FinancialTransactionRepository transactionRepository,
            FinancialMapper financialMapper,
            MonthlyClosingRepository monthlyClosingRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.financialMapper = financialMapper;
        this.monthlyClosingRepository = monthlyClosingRepository;
    }

    @Transactional
    public FinancialTransactionResponse create(FinancialTransactionCreateRequest request) {
        validatePeriodNotClosed(request.transactionDate());

        FinancialTransaction entity = financialMapper.toEntity(request);
        FinancialTransaction saved = transactionRepository.save(entity);
        return financialMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FinancialTransactionResponse findById(UUID id) {
        FinancialTransaction entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento financeiro não encontrado com o ID: " + id));
        return financialMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<FinancialTransactionResponse> findAll(
            TransactionType type,
            TransactionStatus status,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return transactionRepository.findAllWithFilters(type, status, category, startDate, endDate, pageable)
                .map(financialMapper::toResponse);
    }

    @Transactional
    public FinancialTransactionResponse update(UUID id, FinancialTransactionUpdateRequest request) {
        FinancialTransaction entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento financeiro não encontrado com o ID: " + id));

        validatePeriodNotClosed(entity.getTransactionDate());
        validatePeriodNotClosed(request.transactionDate());

        financialMapper.updateEntityFromDto(request, entity);
        FinancialTransaction updated = transactionRepository.save(entity);
        return financialMapper.toResponse(updated);
    }

    @Transactional
    public void cancel(UUID id) {
        FinancialTransaction entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento financeiro não encontrado com o ID: " + id));

        validatePeriodNotClosed(entity.getTransactionDate());

        entity.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(entity);
    }

    @Transactional
    public FinancialTransaction registerIncomeFromAppointment(Appointment appointment) {
        LocalDate transactionDate = appointment.getStartTime().toLocalDate();
        validatePeriodNotClosed(transactionDate);

        Optional<FinancialTransaction> existing = transactionRepository.findByAppointmentId(appointment.getId());
        if (existing.isPresent()) {
            FinancialTransaction transaction = existing.get();
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setAmount(appointment.getEstimatedPrice());
            return transactionRepository.save(transaction);
        }

        FinancialTransaction transaction = new FinancialTransaction(
                appointment,
                null,
                "Atendimento: " + appointment.getService().getName() + " - Cliente: " + appointment.getClient().getName(),
                "SERVICO",
                appointment.getEstimatedPrice(),
                transactionDate,
                TransactionType.INCOME,
                TransactionStatus.COMPLETED,
                "Receita gerada automaticamente pela conclusão do agendamento."
        );

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void cancelTransactionByAppointment(UUID appointmentId) {
        transactionRepository.findByAppointmentId(appointmentId).ifPresent(tx -> {
            tx.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(tx);
        });
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        BigDecimal incomeCompleted = transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.INCOME, TransactionStatus.COMPLETED, start, end);
        BigDecimal expenseCompleted = transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.EXPENSE, TransactionStatus.COMPLETED, start, end);
        BigDecimal pendingIncome = transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.INCOME, TransactionStatus.PENDING, start, end);
        BigDecimal pendingExpense = transactionRepository.sumAmountByTypeAndStatusAndPeriod(TransactionType.EXPENSE, TransactionStatus.PENDING, start, end);

        BigDecimal netOperational = incomeCompleted.subtract(expenseCompleted);

        return new FinancialSummaryResponse(
                start,
                end,
                incomeCompleted,
                expenseCompleted,
                netOperational,
                pendingIncome,
                pendingExpense
        );
    }

    public void validatePeriodNotClosed(LocalDate date) {
        boolean isClosed = monthlyClosingRepository.existsByReferenceYearAndReferenceMonthAndStatus(
                date.getYear(), date.getMonthValue(), MonthlyClosingStatus.CLOSED
        );
        if (isClosed) {
            throw new ClosedPeriodException(
                    "Operação rejeitada: O período contábil de " + date.getMonthValue() + "/" + date.getYear() + " já se encontra encerrado."
            );
        }
    }
}
