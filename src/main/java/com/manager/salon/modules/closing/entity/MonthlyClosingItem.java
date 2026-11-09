package com.manager.salon.modules.closing.entity;

import com.manager.salon.common.BaseEntity;
import com.manager.salon.modules.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_closing_items")
public class MonthlyClosingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_closing_id", nullable = false)
    private MonthlyClosing monthlyClosing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "total_services_count", nullable = false)
    private Integer totalServicesCount = 0;

    @Column(name = "total_gross_generated", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGrossGenerated = BigDecimal.ZERO;

    @Column(name = "total_commission_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionDue = BigDecimal.ZERO;

    public MonthlyClosingItem() {
    }

    public MonthlyClosingItem(MonthlyClosing monthlyClosing, Employee employee, Integer totalServicesCount, BigDecimal totalGrossGenerated, BigDecimal totalCommissionDue) {
        this.monthlyClosing = monthlyClosing;
        this.employee = employee;
        this.totalServicesCount = totalServicesCount;
        this.totalGrossGenerated = totalGrossGenerated;
        this.totalCommissionDue = totalCommissionDue;
    }

    public MonthlyClosing getMonthlyClosing() {
        return monthlyClosing;
    }

    public void setMonthlyClosing(MonthlyClosing monthlyClosing) {
        this.monthlyClosing = monthlyClosing;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getTotalServicesCount() {
        return totalServicesCount;
    }

    public void setTotalServicesCount(Integer totalServicesCount) {
        this.totalServicesCount = totalServicesCount;
    }

    public BigDecimal getTotalGrossGenerated() {
        return totalGrossGenerated;
    }

    public void setTotalGrossGenerated(BigDecimal totalGrossGenerated) {
        this.totalGrossGenerated = totalGrossGenerated;
    }

    public BigDecimal getTotalCommissionDue() {
        return totalCommissionDue;
    }

    public void setTotalCommissionDue(BigDecimal totalCommissionDue) {
        this.totalCommissionDue = totalCommissionDue;
    }
}
