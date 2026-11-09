package com.manager.salon.modules.closing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MonthlyClosingCreateRequest(
        @Schema(description = "Ano de referência do fechamento", example = "2026")
        @NotNull(message = "O ano é obrigatório.")
        @Min(value = 2000, message = "Ano inválido.")
        Integer year,

        @Schema(description = "Mês de referência (1 a 12)", example = "10")
        @NotNull(message = "O mês é obrigatório.")
        @Min(value = 1, message = "O mês deve ser entre 1 e 12.")
        @Max(value = 12, message = "O mês deve ser entre 1 e 12.")
        Integer month
) {}
