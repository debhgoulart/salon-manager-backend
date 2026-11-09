package com.manager.salon.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAdminRequest(
        @Schema(description = "Nome do administrador", example = "Maria Gerente")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome não pode ultrapassar 150 caracteres.")
        String name,

        @Schema(description = "E-mail de acesso", example = "maria@salonmanager.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email,

        @Schema(description = "Senha de acesso", example = "senhaSegura123")
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password
) {}
