package com.manager.salon.modules.appointment.dto;

import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        @Schema(description = "Identificador único do agendamento")
        UUID id,

        @Schema(description = "Identificador do cliente")
        UUID clientId,

        @Schema(description = "Nome do cliente")
        String clientName,

        @Schema(description = "Telefone do cliente")
        String clientPhone,

        @Schema(description = "Identificador do colaborador")
        UUID employeeId,

        @Schema(description = "Nome do colaborador")
        String employeeName,

        @Schema(description = "Identificador do serviço")
        UUID serviceId,

        @Schema(description = "Nome do serviço")
        String serviceName,

        @Schema(description = "Duração do serviço em minutos")
        Integer serviceDurationMinutes,

        @Schema(description = "Data e horário de início")
        OffsetDateTime startTime,

        @Schema(description = "Data e horário de término calculado")
        OffsetDateTime endTime,

        @Schema(description = "Preço previsto do serviço")
        BigDecimal estimatedPrice,

        @Schema(description = "Status atual do agendamento")
        AppointmentStatus status,

        @Schema(description = "Observações")
        String notes,

        @Schema(description = "Versão para controle de concorrência")
        Long version,

        @Schema(description = "Data de criação do agendamento")
        OffsetDateTime createdAt,

        @Schema(description = "Data da última atualização")
        OffsetDateTime updatedAt
) {}
