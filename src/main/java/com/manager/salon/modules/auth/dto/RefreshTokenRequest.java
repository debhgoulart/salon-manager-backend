package com.manager.salon.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "Refresh Token obtido no login")
        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {}
