package com.manager.salon.modules.closing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyClosingItemResponse(
        @Schema(description = "Identificador do item de fechamento")
        UUID id,

        @Schema(description = "Identificador do colaborador")
        UUID employeeId,

        @Schema(description = "Nome do colaborador")
        String employeeName,

        @Schema(description = "Quantidade de serviços realizados no mês")
        Integer totalServicesCount,

        @Schema(description = "Faturamento bruto gerado pelo colaborador")
        BigDecimal totalGrossGenerated,

        @Schema(description = "Total de comissões devidas ao colaborador")
        BigDecimal totalCommissionDue
) {}
