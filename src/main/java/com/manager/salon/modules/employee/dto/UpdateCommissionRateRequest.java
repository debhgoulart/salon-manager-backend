package com.manager.salon.modules.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCommissionRateRequest(
        @Schema(description = "Novo percentual padrão de comissão (0.00 a 100.00)", example = "45.00")
        @NotNull(message = "O percentual de comissão é obrigatório.")
        @DecimalMin(value = "0.00", message = "A comissão não pode ser menor que 0%.")
        @DecimalMax(value = "100.00", message = "A comissão não pode ultrapassar 100%.")
        BigDecimal defaultCommissionRate
) {}
