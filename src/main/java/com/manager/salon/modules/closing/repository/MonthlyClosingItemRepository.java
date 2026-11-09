package com.manager.salon.modules.closing.repository;

import com.manager.salon.modules.closing.entity.MonthlyClosingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonthlyClosingItemRepository extends JpaRepository<MonthlyClosingItem, UUID> {
    List<MonthlyClosingItem> findByMonthlyClosingId(UUID monthlyClosingId);
}
