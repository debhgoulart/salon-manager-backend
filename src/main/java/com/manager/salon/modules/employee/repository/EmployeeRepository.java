package com.manager.salon.modules.employee.repository;

import com.manager.salon.modules.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, UUID id);
    Optional<Employee> findByUserId(UUID userId);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:active IS NULL OR e.active = :active) AND " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Employee> findAllWithFilters(@Param("active") Boolean active, @Param("name") String name, Pageable pageable);
}
