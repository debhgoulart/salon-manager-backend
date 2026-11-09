package com.manager.salon.modules.financial.dto;

import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FinancialTransactionResponse(
        @Schema(description = "Identificador único da transação")
        UUID id,

        @Schema(description = "Identificador do agendamento de origem, se houver")
        UUID appointmentId,

        @Schema(description = "Identificador do fechamento mensal vinculado")
        UUID monthlyClosingId,

        @Schema(description = "Descrição")
        String description,

        @Schema(description = "Categoria")
        String category,

        @Schema(description = "Valor da transação")
        BigDecimal amount,

        @Schema(description = "Data do lançamento")
        LocalDate transactionDate,

        @Schema(description = "Tipo: INCOME ou EXPENSE")
        TransactionType type,

        @Schema(description = "Status: ESTIMATED, PENDING, COMPLETED, CANCELLED")
        TransactionStatus status,

        @Schema(description = "Observações")
        String notes,

        @Schema(description = "Data de registro")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
