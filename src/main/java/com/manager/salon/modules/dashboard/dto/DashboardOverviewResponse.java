package com.manager.salon.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardOverviewResponse(
        @Schema(description = "Data inicial do período analisado")
        LocalDate startDate,

        @Schema(description = "Data final do período analisado")
        LocalDate endDate,

        @Schema(description = "Faturamento bruto de serviços concluídos")
        BigDecimal grossRevenue,

        @Schema(description = "Receitas adicionais avulsas")
        BigDecimal extraRevenue,

        @Schema(description = "Total de despesas operacionais realizadas")
        BigDecimal totalExpenses,

        @Schema(description = "Total de comissões calculadas no período")
        BigDecimal totalCommissions,

        @Schema(description = "Resultado financeiro líquido (Faturamento + Extras - Despesas - Comissões)")
        BigDecimal netProfit,

        @Schema(description = "Total de agendamentos no período")
        Long totalAppointments,

        @Schema(description = "Agendamentos concluídos")
        Long completedAppointments,

        @Schema(description = "Agendamentos cancelados")
        Long cancelledAppointments,

        @Schema(description = "Agendamentos com não comparecimento (No-Show)")
        Long noShowAppointments,

        @Schema(description = "Percentual da taxa de cancelamento e no-show", example = "5.50")
        BigDecimal cancellationRatePercentage
) {}
