package com.manager.salon.modules.commission.dto;

import com.manager.salon.modules.commission.entity.CommissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommissionResponse(
        @Schema(description = "Identificador único da comissão")
        UUID id,

        @Schema(description = "Identificador do agendamento vinculado")
        UUID appointmentId,

        @Schema(description = "Identificador do colaborador")
        UUID employeeId,

        @Schema(description = "Nome do colaborador")
        String employeeName,

        @Schema(description = "Identificador do fechamento mensal, se vinculada")
        UUID monthlyClosingId,

        @Schema(description = "Percentual aplicado no cálculo")
        BigDecimal appliedRate,

        @Schema(description = "Valor total do serviço")
        BigDecimal serviceAmount,

        @Schema(description = "Valor devido ao colaborador a título de comissão")
        BigDecimal commissionAmount,

        @Schema(description = "Valor líquido retido pelo salão")
        BigDecimal salonAmount,

        @Schema(description = "Status da comissão (PENDING, PAID, CANCELLED)")
        CommissionStatus status,

        @Schema(description = "Data e hora do registro")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
