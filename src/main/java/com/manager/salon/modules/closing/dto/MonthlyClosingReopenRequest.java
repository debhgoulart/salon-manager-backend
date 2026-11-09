package com.manager.salon.modules.closing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MonthlyClosingReopenRequest(
        @Schema(description = "Justificativa detalhada para reabertura do mês contábil", example = "Necessário incluir despesa de manutenção retroativa aprovada pela gerência.")
        @NotBlank(message = "A justificativa de reabertura é obrigatória.")
        @Size(min = 10, max = 500, message = "A justificativa deve ter entre 10 e 500 caracteres.")
        String auditNotes
) {}
