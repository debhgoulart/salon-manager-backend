package com.manager.salon.modules.appointment.controller;

import com.manager.salon.modules.appointment.dto.AppointmentCreateRequest;
import com.manager.salon.modules.appointment.dto.AppointmentResponse;
import com.manager.salon.modules.appointment.dto.AppointmentUpdateRequest;
import com.manager.salon.modules.appointment.dto.TimeSlotResponse;
import com.manager.salon.modules.appointment.dto.UpdateAppointmentStatusRequest;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.appointment.service.AppointmentService;
import com.manager.salon.modules.appointment.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Agendamentos", description = "Endpoints para agendamento de serviços, controle de horários e disponibilidade")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    public AppointmentController(AppointmentService appointmentService, AvailabilityService availabilityService) {
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento, validando conflitos de agenda e calculando o horário de término automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso."),
            @ApiResponse(responseCode = "409", description = "Conflito de horário para o colaborador."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou cliente/colaborador inativo.")
    })
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos", description = "Lista agendamentos com filtros por colaborador, cliente, status e período.")
    public ResponseEntity<Page<AppointmentResponse>> findAll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable
    ) {
        Page<AppointmentResponse> page = appointmentService.findAll(employeeId, clientId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID", description = "Retorna detalhes completos do agendamento.")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reagendar / atualizar agendamento", description = "Altera horário de início ou serviço, revalidando conflitos de agenda.")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentUpdateRequest request
    ) {
        AppointmentResponse response = appointmentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do agendamento", description = "Avança o status do agendamento conforme a máquina de estados permitida.")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentResponse response = appointmentService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar agendamento", description = "Cancela o agendamento.")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason
    ) {
        appointmentService.cancel(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    @Operation(summary = "Consultar slots disponíveis", description = "Retorna os intervalos de horários disponíveis para determinado colaborador e serviço na data informada.")
    public ResponseEntity<List<TimeSlotResponse>> getAvailability(
            @RequestParam UUID employeeId,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TimeSlotResponse> slots = availabilityService.getAvailableSlots(employeeId, serviceId, date, null);
        return ResponseEntity.ok(slots);
    }
}
