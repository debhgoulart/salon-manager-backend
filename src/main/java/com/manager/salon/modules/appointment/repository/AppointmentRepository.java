package com.manager.salon.modules.appointment.repository;

import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
import com.manager.salon.modules.dashboard.dto.TopEmployeeResponse;
import com.manager.salon.modules.dashboard.dto.TopServiceResponse;
import com.manager.salon.modules.dashboard.dto.UpcomingAppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.employee.id = :employeeId " +
           "AND (:excludeId IS NULL OR a.id != :excludeId) " +
           "AND a.status NOT IN ('CANCELADO', 'NAO_COMPARECEU') " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findOverlappingAppointments(
            @Param("employeeId") UUID employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("excludeId") UUID excludeId
    );

    @Query("SELECT a FROM Appointment a WHERE a.employee.id = :employeeId " +
           "AND a.startTime >= :startOfDay AND a.startTime < :endOfDay " +
           "AND a.status NOT IN ('CANCELADO', 'NAO_COMPARECEU') " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findActiveByEmployeeAndDay(
            @Param("employeeId") UUID employeeId,
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.client " +
           "JOIN FETCH a.employee " +
           "JOIN FETCH a.service " +
           "WHERE (:employeeId IS NULL OR a.employee.id = :employeeId) " +
           "AND (:clientId IS NULL OR a.client.id = :clientId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (cast(:startDate as timestamp) IS NULL OR a.startTime >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR a.endTime <= :endDate)")
    Page<Appointment> findAllWithFilters(
            @Param("employeeId") UUID employeeId,
            @Param("clientId") UUID clientId,
            @Param("status") AppointmentStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable
    );

    List<Appointment> findByClientIdOrderByStartTimeDesc(UUID clientId);

    long countByStartTimeBetween(OffsetDateTime start, OffsetDateTime end);
    long countByStatusAndStartTimeBetween(AppointmentStatus status, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT new com.manager.salon.modules.dashboard.dto.TopServiceResponse(" +
           "a.service.id, a.service.name, COUNT(a), SUM(a.estimatedPrice)) " +
           "FROM Appointment a " +
           "WHERE a.status = 'CONCLUIDO' AND a.startTime >= :start AND a.startTime <= :end " +
           "GROUP BY a.service.id, a.service.name " +
           "ORDER BY COUNT(a) DESC")
    List<TopServiceResponse> findTopServices(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable
    );

    @Query("SELECT new com.manager.salon.modules.dashboard.dto.TopEmployeeResponse(" +
           "a.employee.id, a.employee.name, COUNT(a), SUM(a.estimatedPrice)) " +
           "FROM Appointment a " +
           "WHERE a.status = 'CONCLUIDO' AND a.startTime >= :start AND a.startTime <= :end " +
           "GROUP BY a.employee.id, a.employee.name " +
           "ORDER BY SUM(a.estimatedPrice) DESC")
    List<TopEmployeeResponse> findTopEmployees(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable
    );

    @Query("SELECT new com.manager.salon.modules.dashboard.dto.UpcomingAppointmentResponse(" +
           "a.id, a.client.name, a.employee.name, a.service.name, a.startTime, a.endTime, a.estimatedPrice, a.status) " +
           "FROM Appointment a " +
           "WHERE a.startTime >= :now AND a.status IN ('AGENDADO', 'CONFIRMADO') " +
           "ORDER BY a.startTime ASC")
    List<UpcomingAppointmentResponse> findUpcomingAppointments(
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
