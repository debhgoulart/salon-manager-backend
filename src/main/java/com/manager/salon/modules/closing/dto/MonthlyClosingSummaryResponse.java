package com.manager.salon.modules.closing.dto;

import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MonthlyClosingSummaryResponse(
        @Schema(description = "Identificador único do fechamento")
        UUID id,

        @Schema(description = "Ano de referência")
        Integer referenceYear,

        @Schema(description = "Mês de referência")
        Integer referenceMonth,

        @Schema(description = "Faturamento bruto total")
        BigDecimal grossRevenue,

        @Schema(description = "Total de despesas")
        BigDecimal totalExpenses,

        @Schema(description = "Total de comissões")
        BigDecimal totalCommissions,

        @Schema(description = "Resultado líquido do salão")
        BigDecimal netResult,

        @Schema(description = "Status do fechamento")
        MonthlyClosingStatus status,

        @Schema(description = "Data de encerramento")
        OffsetDateTime closedAt
) {}
