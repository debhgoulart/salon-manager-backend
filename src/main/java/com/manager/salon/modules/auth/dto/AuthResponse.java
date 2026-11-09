package com.manager.salon.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Token JWT para autorização em requisições subsequentes")
        String accessToken,

        @Schema(description = "Refresh Token para renovar o acesso")
        String refreshToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de expiração do Access Token em segundos", example = "7200")
        long expiresInSeconds,

        @Schema(description = "Dados do usuário autenticado")
        UserResponse user
) {}
