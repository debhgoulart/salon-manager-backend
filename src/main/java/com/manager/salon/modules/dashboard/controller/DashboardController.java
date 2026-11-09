package com.manager.salon.modules.dashboard.controller;

import com.manager.salon.modules.dashboard.dto.DashboardOverviewResponse;
import com.manager.salon.modules.dashboard.dto.TopEmployeeResponse;
import com.manager.salon.modules.dashboard.dto.TopServiceResponse;
import com.manager.salon.modules.dashboard.dto.UpcomingAppointmentResponse;
import com.manager.salon.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard", description = "Endpoints para indicadores estratégicos, métricas gerenciais e relatórios executivos")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Visão geral e KPIs financeiros", description = "Retorna os principais indicadores consolidados de faturamento, despesas, comissões, margem líquida e taxa de cancelamentos no período.")
    public ResponseEntity<DashboardOverviewResponse> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        DashboardOverviewResponse response = dashboardService.getOverview(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-services")
    @Operation(summary = "Ranking dos serviços mais realizados", description = "Retorna os serviços mais procurados no período ordenados por quantidade e faturamento gerado.")
    public ResponseEntity<List<TopServiceResponse>> getTopServices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<TopServiceResponse> list = dashboardService.getTopServices(startDate, endDate, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/top-employees")
    @Operation(summary = "Ranking de colaboradores mais produtivos", description = "Retorna os colaboradores com maior faturamento gerado e atendimentos concluídos.")
    public ResponseEntity<List<TopEmployeeResponse>> getTopEmployees(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<TopEmployeeResponse> list = dashboardService.getTopEmployees(startDate, endDate, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/upcoming-appointments")
    @Operation(summary = "Próximos agendamentos do salão", description = "Retorna a fila cronológica dos próximos atendimentos previstos.")
    public ResponseEntity<List<UpcomingAppointmentResponse>> getUpcomingAppointments(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<UpcomingAppointmentResponse> list = dashboardService.getUpcomingAppointments(limit);
        return ResponseEntity.ok(list);
    }
}
