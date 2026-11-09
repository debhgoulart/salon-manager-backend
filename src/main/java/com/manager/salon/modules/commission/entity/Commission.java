package com.manager.salon.modules.commission.entity;

import com.manager.salon.common.BaseEntity;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "commissions")
public class Commission extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "monthly_closing_id")
    private UUID monthlyClosingId;

    @Column(name = "applied_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal appliedRate;

    @Column(name = "service_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal serviceAmount;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "salon_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal salonAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    public Commission() {
    }

    public Commission(Appointment appointment, Employee employee, UUID monthlyClosingId, BigDecimal appliedRate, BigDecimal serviceAmount, BigDecimal commissionAmount, BigDecimal salonAmount, CommissionStatus status) {
        this.appointment = appointment;
        this.employee = employee;
        this.monthlyClosingId = monthlyClosingId;
        this.appliedRate = appliedRate;
        this.serviceAmount = serviceAmount;
        this.commissionAmount = commissionAmount;
        this.salonAmount = salonAmount;
        this.status = status != null ? status : CommissionStatus.PENDING;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public UUID getMonthlyClosingId() {
        return monthlyClosingId;
    }

    public void setMonthlyClosingId(UUID monthlyClosingId) {
        this.monthlyClosingId = monthlyClosingId;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public void setAppliedRate(BigDecimal appliedRate) {
        this.appliedRate = appliedRate;
    }

    public BigDecimal getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(BigDecimal serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getSalonAmount() {
        return salonAmount;
    }

    public void setSalonAmount(BigDecimal salonAmount) {
        this.salonAmount = salonAmount;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionStatus status) {
        this.status = status;
    }
}
