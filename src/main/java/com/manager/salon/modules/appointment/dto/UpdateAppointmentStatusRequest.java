package com.manager.salon.modules.appointment.dto;

import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @Schema(description = "Novo status do agendamento")
        @NotNull(message = "O status é obrigatório.")
        AppointmentStatus status,

        @Schema(description = "Justificativa ou notas sobre a transição de status", example = "Cliente avisou que não poderá comparecer.")
        String notes
) {}
