package com.manager.salon.modules.service.repository;

import com.manager.salon.modules.service.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    @Query("SELECT s FROM ServiceEntity s WHERE " +
           "(:active IS NULL OR s.active = :active) AND " +
           "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ServiceEntity> searchServices(@Param("active") Boolean active, @Param("name") String name, Pageable pageable);

    List<ServiceEntity> findByActiveTrue();
}
