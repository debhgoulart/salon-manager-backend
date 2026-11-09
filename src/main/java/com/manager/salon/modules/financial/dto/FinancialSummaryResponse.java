package com.manager.salon.modules.financial.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialSummaryResponse(
        @Schema(description = "Data inicial do período analisado")
        LocalDate startDate,

        @Schema(description = "Data final do período analisado")
        LocalDate endDate,

        @Schema(description = "Total de receitas efetivamente recebidas (status COMPLETED)")
        BigDecimal totalIncomeCompleted,

        @Schema(description = "Total de despesas efetivamente pagas (status COMPLETED)")
        BigDecimal totalExpenseCompleted,

        @Schema(description = "Resultado operacional líquido realizado (Receitas - Despesas)")
        BigDecimal netOperationalResult,

        @Schema(description = "Receitas previstas ou pendentes")
        BigDecimal pendingIncome,

        @Schema(description = "Despesas previstas ou pendentes")
        BigDecimal pendingExpense
) {}
