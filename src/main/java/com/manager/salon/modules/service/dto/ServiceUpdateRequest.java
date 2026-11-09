package com.manager.salon.modules.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceUpdateRequest(
        @Schema(description = "Nome do serviço prestado", example = "Corte Feminino")
        @NotBlank(message = "O nome do serviço é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String name,

        @Schema(description = "Descrição detalhada do serviço", example = "Corte com lavagem e secagem inclusas")
        String description,

        @Schema(description = "Preço do serviço em reais", example = "130.00")
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        BigDecimal price,

        @Schema(description = "Duração prevista em minutos", example = "50")
        @NotNull(message = "A duração do serviço é obrigatória.")
        @Min(value = 5, message = "A duração mínima é de 5 minutos.")
        @Max(value = 480, message = "A duração máxima é de 480 minutos (8 horas).")
        Integer durationMinutes,

        @Schema(description = "Taxa de comissão específica (opcional)", example = "50.00")
        @DecimalMin(value = "0.00", message = "A taxa de comissão não pode ser menor que 0%.")
        @DecimalMax(value = "100.00", message = "A taxa de comissão não pode exceder 100%.")
        BigDecimal customCommissionRate
) {}
