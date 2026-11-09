package com.manager.salon.modules.closing.repository;

import com.manager.salon.modules.closing.entity.MonthlyClosing;
import com.manager.salon.modules.closing.entity.MonthlyClosingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyClosingRepository extends JpaRepository<MonthlyClosing, UUID> {
    Optional<MonthlyClosing> findByReferenceYearAndReferenceMonth(Integer referenceYear, Integer referenceMonth);
    boolean existsByReferenceYearAndReferenceMonthAndStatus(Integer referenceYear, Integer referenceMonth, MonthlyClosingStatus status);
}
