package com.manager.salon.modules.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @Schema(description = "Nome completo do cliente", example = "Ana Clara Lima")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String name,

        @Schema(description = "Telefone para contato", example = "(11) 91234-5678")
        @NotBlank(message = "O telefone é obrigatório.")
        String phone,

        @Schema(description = "E-mail do cliente (opcional)", example = "ana.lima@email.com")
        @Email(message = "O e-mail deve ter um formato válido.")
        String email,

        @Schema(description = "Observações ou preferências", example = "Alergia a esmalte com tolueno.")
        String notes
) {}
