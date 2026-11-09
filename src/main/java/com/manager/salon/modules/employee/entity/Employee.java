package com.manager.salon.modules.employee.entity;

import com.manager.salon.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "role_title", nullable = false, length = 100)
    private String roleTitle;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "default_commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultCommissionRate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Employee() {
    }

    public Employee(UUID userId, String name, String cpf, String phone, String email, String roleTitle, LocalDate hireDate, BigDecimal defaultCommissionRate, boolean active) {
        this.userId = userId;
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.roleTitle = roleTitle;
        this.hireDate = hireDate;
        this.defaultCommissionRate = defaultCommissionRate;
        this.active = active;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getDefaultCommissionRate() {
        return defaultCommissionRate;
    }

    public void setDefaultCommissionRate(BigDecimal defaultCommissionRate) {
        this.defaultCommissionRate = defaultCommissionRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
