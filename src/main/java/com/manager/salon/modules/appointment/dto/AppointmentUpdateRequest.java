package com.manager.salon.modules.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentUpdateRequest(
        @Schema(description = "Novo identificador de serviço (opcional)")
        UUID serviceId,

        @Schema(description = "Nova data e horário de início do atendimento", example = "2026-11-15T15:00:00-03:00")
        @NotNull(message = "O horário de início é obrigatório.")
        @Future(message = "O novo horário de início deve estar no futuro.")
        OffsetDateTime startTime,

        @Schema(description = "Observações atualizadas")
        String notes
) {}
