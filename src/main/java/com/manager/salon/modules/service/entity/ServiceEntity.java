package com.manager.salon.modules.service.entity;

import com.manager.salon.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class ServiceEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "custom_commission_rate", precision = 5, scale = 2)
    private BigDecimal customCommissionRate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ServiceEntity() {
    }

    public ServiceEntity(String name, String description, BigDecimal price, Integer durationMinutes, BigDecimal customCommissionRate, boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.customCommissionRate = customCommissionRate;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getCustomCommissionRate() {
        return customCommissionRate;
    }

    public void setCustomCommissionRate(BigDecimal customCommissionRate) {
        this.customCommissionRate = customCommissionRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
