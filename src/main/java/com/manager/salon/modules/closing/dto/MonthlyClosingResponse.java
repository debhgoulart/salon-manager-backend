package com.manager.salon.modules.closing.dto;

import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MonthlyClosingResponse(
        @Schema(description = "Identificador único do fechamento")
        UUID id,

        @Schema(description = "Ano de referência")
        Integer referenceYear,

        @Schema(description = "Mês de referência")
        Integer referenceMonth,

        @Schema(description = "Faturamento bruto de serviços")
        BigDecimal grossRevenue,

        @Schema(description = "Receitas adicionais avulsas")
        BigDecimal extraRevenue,

        @Schema(description = "Total de despesas operacionais")
        BigDecimal totalExpenses,

        @Schema(description = "Total de comissões pagas/devidas aos colaboradores")
        BigDecimal totalCommissions,

        @Schema(description = "Resultado financeiro líquido retido pelo salão")
        BigDecimal netResult,

        @Schema(description = "Status do fechamento (CLOSED, REOPENED)")
        MonthlyClosingStatus status,

        @Schema(description = "Data e hora de encerramento")
        OffsetDateTime closedAt,

        @Schema(description = "Usuário responsável pelo encerramento")
        String closedBy,

        @Schema(description = "Notas de auditoria, se houver reabertura")
        String auditNotes,

        @Schema(description = "Demonstrativo consolidado por colaborador")
        List<MonthlyClosingItemResponse> items
) {}
