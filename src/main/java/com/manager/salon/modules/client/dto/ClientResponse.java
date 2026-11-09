package com.manager.salon.modules.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
        @Schema(description = "Identificador único do cliente")
        UUID id,

        @Schema(description = "Nome do cliente")
        String name,

        @Schema(description = "Telefone")
        String phone,

        @Schema(description = "E-mail")
        String email,

        @Schema(description = "Data de cadastro")
        LocalDate registrationDate,

        @Schema(description = "Observações")
        String notes,

        @Schema(description = "Status ativo/inativo")
        boolean active,

        @Schema(description = "Data de criação")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
