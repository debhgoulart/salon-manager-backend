package com.manager.salon.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record TopServiceResponse(
        @Schema(description = "Identificador do serviço")
        UUID serviceId,

        @Schema(description = "Nome do serviço")
        String serviceName,

        @Schema(description = "Quantidade de atendimentos realizados")
        Long appointmentsCount,

        @Schema(description = "Faturamento total gerado pelo serviço")
        BigDecimal totalGeneratedRevenue
) {}
