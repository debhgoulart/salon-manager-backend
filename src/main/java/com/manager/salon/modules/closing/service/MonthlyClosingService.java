package com.manager.salon.modules.closing.service;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ClosedPeriodException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.closing.dto.MonthlyClosingCreateRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingReopenRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingResponse;
import com.manager.salon.modules.closing.dto.MonthlyClosingSummaryResponse;
import com.manager.salon.modules.closing.entity.MonthlyClosing;
import com.manager.salon.modules.closing.entity.MonthlyClosingItem;
import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import com.manager.salon.modules.closing.mapper.MonthlyClosingMapper;
import com.manager.salon.modules.closing.repository.MonthlyClosingRepository;
import com.manager.salon.modules.commission.entity.Commission;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.financial.entity.FinancialTransaction;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MonthlyClosingService {

    private final MonthlyClosingRepository monthlyClosingRepository;
    private final MonthlyClosingMapper monthlyClosingMapper;
    private final FinancialTransactionRepository transactionRepository;
    private final CommissionRepository commissionRepository;

    public MonthlyClosingService(
            MonthlyClosingRepository monthlyClosingRepository,
            MonthlyClosingMapper monthlyClosingMapper,
            FinancialTransactionRepository transactionRepository,
            CommissionRepository commissionRepository
    ) {
        this.monthlyClosingRepository = monthlyClosingRepository;
        this.monthlyClosingMapper = monthlyClosingMapper;
        this.transactionRepository = transactionRepository;
        this.commissionRepository = commissionRepository;
    }

    @Transactional
    public MonthlyClosingResponse closeMonth(MonthlyClosingCreateRequest request, String closedBy) {
        int year = request.year();
        int month = request.month();

        Optional<MonthlyClosing> existing = monthlyClosingRepository.findByReferenceYearAndReferenceMonth(year, month);
        if (existing.isPresent() && existing.get().getStatus() == MonthlyClosingStatus.CLOSED) {
            throw new ClosedPeriodException("O fechamento para " + month + "/" + year + " já foi realizado e está encerrado.");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        OffsetDateTime startOffset = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOffset = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        // 1. Consolida Transações Financeiras Concluídas no Período
        List<FinancialTransaction> transactions = transactionRepository.findCompletedUnclosedForPeriod(startDate, endDate);

        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal extraRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (FinancialTransaction tx : transactions) {
            if (tx.getType() == TransactionType.INCOME) {
                if ("SERVICO".equalsIgnoreCase(tx.getCategory())) {
                    grossRevenue = grossRevenue.add(tx.getAmount());
                } else {
                    extraRevenue = extraRevenue.add(tx.getAmount());
                }
            } else if (tx.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(tx.getAmount());
            }
        }

        // 2. Consolida Comissões Pendentes no Período
        List<Commission> commissions = commissionRepository.findPendingForClosing(startOffset, endOffset);

        Map<Employee, List<Commission>> commissionsByEmployee = commissions.stream()
                .collect(Collectors.groupingBy(Commission::getEmployee));

        MonthlyClosing closing = existing.orElseGet(MonthlyClosing::new);
        closing.setReferenceYear(year);
        closing.setReferenceMonth(month);
        closing.setGrossRevenue(grossRevenue);
        closing.setExtraRevenue(extraRevenue);
        closing.setTotalExpenses(totalExpenses);
        closing.setStatus(MonthlyClosingStatus.CLOSED);
        closing.setClosedAt(OffsetDateTime.now());
        closing.setClosedBy(closedBy != null ? closedBy : "SYSTEM");

        List<MonthlyClosingItem> items = new ArrayList<>();
        BigDecimal totalCommissions = BigDecimal.ZERO;

        for (Map.Entry<Employee, List<Commission>> entry : commissionsByEmployee.entrySet()) {
            Employee emp = entry.getKey();
            List<Commission> empCommissions = entry.getValue();

            int servicesCount = empCommissions.size();
            BigDecimal totalGross = empCommissions.stream()
                    .map(Commission::getServiceAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDue = empCommissions.stream()
                    .map(Commission::getCommissionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalCommissions = totalCommissions.add(totalDue);

            MonthlyClosingItem item = new MonthlyClosingItem(closing, emp, servicesCount, totalGross, totalDue);
            items.add(item);
        }

        closing.setTotalCommissions(totalCommissions);
        BigDecimal netResult = grossRevenue.add(extraRevenue).subtract(totalExpenses).subtract(totalCommissions);
        closing.setNetResult(netResult);

        closing.getItems().clear();
        closing.getItems().addAll(items);

        MonthlyClosing savedClosing = monthlyClosingRepository.save(closing);

        // 3. Vincula fechamento aos lançamentos e comissões consolidados
        for (FinancialTransaction tx : transactions) {
            tx.setMonthlyClosingId(savedClosing.getId());
            transactionRepository.save(tx);
        }

        for (Commission comm : commissions) {
            comm.setMonthlyClosingId(savedClosing.getId());
            commissionRepository.save(comm);
        }

        return monthlyClosingMapper.toResponse(savedClosing);
    }

    @Transactional
    public MonthlyClosingResponse reopen(UUID id, MonthlyClosingReopenRequest request, String reopenedBy) {
        MonthlyClosing closing = monthlyClosingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fechamento mensal não encontrado com o ID: " + id));

        if (closing.getStatus() == MonthlyClosingStatus.REOPENED) {
            throw new BusinessException("Este fechamento mensal já se encontra reaberto.");
        }

        closing.setStatus(MonthlyClosingStatus.REOPENED);
        String auditEntry = "[" + OffsetDateTime.now() + " por " + (reopenedBy != null ? reopenedBy : "ADMIN") + "]: " + request.auditNotes();
        closing.setAuditNotes(closing.getAuditNotes() != null ? closing.getAuditNotes() + "\n" + auditEntry : auditEntry);

        MonthlyClosing updated = monthlyClosingRepository.save(closing);
        return monthlyClosingMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public MonthlyClosingResponse findById(UUID id) {
        MonthlyClosing closing = monthlyClosingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fechamento mensal não encontrado com o ID: " + id));
        return monthlyClosingMapper.toResponse(closing);
    }

    @Transactional(readOnly = true)
    public Page<MonthlyClosingSummaryResponse> findAll(Pageable pageable) {
        return monthlyClosingRepository.findAll(pageable)
                .map(monthlyClosingMapper::toSummaryResponse);
    }
}
