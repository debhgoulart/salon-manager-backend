package com.manager.salon.modules.client.repository;

import com.manager.salon.modules.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("SELECT c FROM Client c WHERE " +
           "(:active IS NULL OR c.active = :active) AND " +
           "(:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR c.phone LIKE CONCAT('%', :query, '%') " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Client> searchClients(@Param("active") Boolean active, @Param("query") String query, Pageable pageable);
}
