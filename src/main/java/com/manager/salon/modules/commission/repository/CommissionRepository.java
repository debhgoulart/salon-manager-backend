package com.manager.salon.modules.commission.repository;

import com.manager.salon.modules.commission.entity.Commission;
import com.manager.salon.modules.commission.entity.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    Optional<Commission> findByAppointmentId(UUID appointmentId);

    @Query("SELECT c FROM Commission c " +
           "JOIN FETCH c.employee " +
           "JOIN FETCH c.appointment " +
           "WHERE (:employeeId IS NULL OR c.employee.id = :employeeId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (cast(:startDate as timestamp) IS NULL OR c.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR c.createdAt <= :endDate)")
    Page<Commission> findAllWithFilters(
            @Param("employeeId") UUID employeeId,
            @Param("status") CommissionStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT c FROM Commission c WHERE c.employee.id = :employeeId " +
           "AND c.status = 'PENDING' AND c.monthlyClosingId IS NULL " +
           "AND c.createdAt >= :startDate AND c.createdAt < :endDate")
    List<Commission> findPendingForClosingByEmployee(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    @Query("SELECT c FROM Commission c WHERE c.monthlyClosingId IS NULL " +
           "AND c.status = 'PENDING' " +
           "AND c.createdAt >= :startDate AND c.createdAt < :endDate")
    List<Commission> findPendingForClosing(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM Commission c " +
           "WHERE c.status != 'CANCELLED' " +
           "AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
    BigDecimal sumCommissionsByPeriod(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
}
