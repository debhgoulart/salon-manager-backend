package com.manager.salon.modules.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeResponse(
        @Schema(description = "Identificador único do colaborador")
        UUID id,

        @Schema(description = "Identificador do usuário de login, se vinculado")
        UUID userId,

        @Schema(description = "Nome do colaborador")
        String name,

        @Schema(description = "CPF")
        String cpf,

        @Schema(description = "Telefone")
        String phone,

        @Schema(description = "E-mail")
        String email,

        @Schema(description = "Cargo ou especialidade")
        String roleTitle,

        @Schema(description = "Data de contratação")
        LocalDate hireDate,

        @Schema(description = "Percentual padrão de comissão")
        BigDecimal defaultCommissionRate,

        @Schema(description = "Status ativo/inativo")
        boolean active,

        @Schema(description = "Data de criação")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
