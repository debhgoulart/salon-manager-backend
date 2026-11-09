package com.manager.salon.modules.financial.repository;

import com.manager.salon.modules.financial.entity.FinancialTransaction;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    Optional<FinancialTransaction> findByAppointmentId(UUID appointmentId);

    @Query("SELECT f FROM FinancialTransaction f WHERE " +
           "(:type IS NULL OR f.type = :type) AND " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:category IS NULL OR LOWER(f.category) = LOWER(:category)) AND " +
           "(cast(:startDate as date) IS NULL OR f.transactionDate >= :startDate) AND " +
           "(cast(:endDate as date) IS NULL OR f.transactionDate <= :endDate)")
    Page<FinancialTransaction> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialTransaction f WHERE " +
           "f.type = :type AND f.status = :status AND " +
           "f.transactionDate >= :startDate AND f.transactionDate <= :endDate")
    BigDecimal sumAmountByTypeAndStatusAndPeriod(
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialTransaction f WHERE " +
           "f.type = :type AND f.status = :status AND LOWER(f.category) = LOWER(:category) AND " +
           "f.transactionDate >= :startDate AND f.transactionDate <= :endDate")
    BigDecimal sumAmountByTypeAndCategoryAndStatus(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT f FROM FinancialTransaction f WHERE " +
           "f.transactionDate >= :startDate AND f.transactionDate <= :endDate AND " +
           "f.status = 'COMPLETED' AND f.monthlyClosingId IS NULL")
    List<FinancialTransaction> findCompletedUnclosedForPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
