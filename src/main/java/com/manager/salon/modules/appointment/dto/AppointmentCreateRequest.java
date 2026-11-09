package com.manager.salon.modules.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentCreateRequest(
        @Schema(description = "Identificador do cliente")
        @NotNull(message = "O ID do cliente é obrigatório.")
        UUID clientId,

        @Schema(description = "Identificador do colaborador")
        @NotNull(message = "O ID do colaborador é obrigatório.")
        UUID employeeId,

        @Schema(description = "Identificador do serviço a ser realizado")
        @NotNull(message = "O ID do serviço é obrigatório.")
        UUID serviceId,

        @Schema(description = "Data e horário de início do atendimento (ISO-8601)", example = "2026-11-15T14:00:00-03:00")
        @NotNull(message = "O horário de início é obrigatório.")
        @Future(message = "O horário de início deve estar no futuro.")
        OffsetDateTime startTime,

        @Schema(description = "Observações sobre o agendamento", example = "Cliente prefere tintura sem amônia.")
        String notes
) {}
