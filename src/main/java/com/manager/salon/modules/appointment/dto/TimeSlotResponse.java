package com.manager.salon.modules.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record TimeSlotResponse(
        @Schema(description = "Horário de início do intervalo", example = "2026-11-15T09:00:00-03:00")
        OffsetDateTime startTime,

        @Schema(description = "Horário de término do intervalo", example = "2026-11-15T09:45:00-03:00")
        OffsetDateTime endTime,

        @Schema(description = "Indica se o horário está livre para reserva", example = "true")
        boolean available
) {}
