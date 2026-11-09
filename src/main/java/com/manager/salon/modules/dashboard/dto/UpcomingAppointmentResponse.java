package com.manager.salon.modules.dashboard.dto;

import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpcomingAppointmentResponse(
        @Schema(description = "Identificador do agendamento")
        UUID appointmentId,

        @Schema(description = "Nome do cliente")
        String clientName,

        @Schema(description = "Nome do colaborador")
        String employeeName,

        @Schema(description = "Nome do serviço")
        String serviceName,

        @Schema(description = "Horário de início")
        OffsetDateTime startTime,

        @Schema(description = "Horário de término")
        OffsetDateTime endTime,

        @Schema(description = "Preço previsto")
        BigDecimal estimatedPrice,

        @Schema(description = "Status do agendamento")
        AppointmentStatus status
) {}
