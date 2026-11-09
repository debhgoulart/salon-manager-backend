package com.manager.salon.modules.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
        @Schema(description = "Identificador único do serviço")
        UUID id,

        @Schema(description = "Nome do serviço")
        String name,

        @Schema(description = "Descrição detalhada")
        String description,

        @Schema(description = "Preço")
        BigDecimal price,

        @Schema(description = "Duração em minutos")
        Integer durationMinutes,

        @Schema(description = "Taxa de comissão específica, se houver")
        BigDecimal customCommissionRate,

        @Schema(description = "Status ativo/inativo")
        boolean active,

        @Schema(description = "Data de criação")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
