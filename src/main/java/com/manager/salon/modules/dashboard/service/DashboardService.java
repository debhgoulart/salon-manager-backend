package com.manager.salon.modules.dashboard.service;

import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.appointment.repository.AppointmentRepository;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import com.manager.salon.modules.dashboard.dto.DashboardOverviewResponse;
import com.manager.salon.modules.dashboard.dto.TopEmployeeResponse;
import com.manager.salon.modules.dashboard.dto.TopServiceResponse;
import com.manager.salon.modules.dashboard.dto.UpcomingAppointmentResponse;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.repository.FinancialTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final CommissionRepository commissionRepository;

    public DashboardService(
            AppointmentRepository appointmentRepository,
            FinancialTransactionRepository transactionRepository,
            CommissionRepository commissionRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.transactionRepository = transactionRepository;
        this.commissionRepository = commissionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        OffsetDateTime startOffset = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOffset = end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        BigDecimal grossRevenue = transactionRepository.sumAmountByTypeAndCategoryAndStatus(
                TransactionType.INCOME, "SERVICO", TransactionStatus.COMPLETED, start, end
        );
        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndStatusAndPeriod(
                TransactionType.INCOME, TransactionStatus.COMPLETED, start, end
        );
        BigDecimal extraRevenue = totalIncome.subtract(grossRevenue);

        BigDecimal totalExpenses = transactionRepository.sumAmountByTypeAndStatusAndPeriod(
                TransactionType.EXPENSE, TransactionStatus.COMPLETED, start, end
        );
        BigDecimal totalCommissions = commissionRepository.sumCommissionsByPeriod(startOffset, endOffset);

        BigDecimal netProfit = grossRevenue.add(extraRevenue).subtract(totalExpenses).subtract(totalCommissions);

        long totalAppointments = appointmentRepository.countByStartTimeBetween(startOffset, endOffset);
        long completed = appointmentRepository.countByStatusAndStartTimeBetween(AppointmentStatus.CONCLUIDO, startOffset, endOffset);
        long cancelled = appointmentRepository.countByStatusAndStartTimeBetween(AppointmentStatus.CANCELADO, startOffset, endOffset);
        long noShow = appointmentRepository.countByStatusAndStartTimeBetween(AppointmentStatus.NAO_COMPARECEU, startOffset, endOffset);

        BigDecimal cancellationRate = BigDecimal.ZERO;
        if (totalAppointments > 0) {
            long totalUnfulfilled = cancelled + noShow;
            cancellationRate = BigDecimal.valueOf(totalUnfulfilled)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAppointments), 2, RoundingMode.HALF_EVEN);
        }

        return new DashboardOverviewResponse(
                start,
                end,
                grossRevenue,
                extraRevenue,
                totalExpenses,
                totalCommissions,
                netProfit,
                totalAppointments,
                completed,
                cancelled,
                noShow,
                cancellationRate
        );
    }

    @Transactional(readOnly = true)
    public List<TopServiceResponse> getTopServices(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        OffsetDateTime startOffset = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOffset = end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        int maxResults = limit > 0 ? limit : 5;
        return appointmentRepository.findTopServices(startOffset, endOffset, PageRequest.of(0, maxResults));
    }

    @Transactional(readOnly = true)
    public List<TopEmployeeResponse> getTopEmployees(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        OffsetDateTime startOffset = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOffset = end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        int maxResults = limit > 0 ? limit : 5;
        return appointmentRepository.findTopEmployees(startOffset, endOffset, PageRequest.of(0, maxResults));
    }

    @Transactional(readOnly = true)
    public List<UpcomingAppointmentResponse> getUpcomingAppointments(int limit) {
        int maxResults = limit > 0 ? limit : 5;
        return appointmentRepository.findUpcomingAppointments(OffsetDateTime.now(), PageRequest.of(0, maxResults));
    }
}
