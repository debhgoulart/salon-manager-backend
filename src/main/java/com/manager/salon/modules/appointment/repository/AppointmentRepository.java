package com.manager.salon.modules.appointment.repository;

import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.appointment.entity.AppointmentStatus;
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
}
