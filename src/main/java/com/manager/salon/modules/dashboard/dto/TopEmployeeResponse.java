package com.manager.salon.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record TopEmployeeResponse(
        @Schema(description = "Identificador do colaborador")
        UUID employeeId,

        @Schema(description = "Nome do colaborador")
        String employeeName,

        @Schema(description = "Quantidade de atendimentos realizados")
        Long appointmentsCount,

        @Schema(description = "Faturamento total gerado pelo colaborador")
        BigDecimal totalGeneratedRevenue
) {}
