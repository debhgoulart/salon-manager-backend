package com.manager.salon.modules.auth.dto;

import com.manager.salon.modules.auth.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserResponse(
        @Schema(description = "Identificador único do usuário")
        UUID id,

        @Schema(description = "Nome do usuário")
        String name,

        @Schema(description = "E-mail de acesso")
        String email,

        @Schema(description = "Perfil de acesso")
        UserRole role,

        @Schema(description = "Status do usuário")
        boolean active
) {}
