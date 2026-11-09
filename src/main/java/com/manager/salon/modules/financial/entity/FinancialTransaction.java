package com.manager.salon.modules.financial.entity;

import com.manager.salon.common.BaseEntity;
import com.manager.salon.modules.appointment.entity.Appointment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "monthly_closing_id")
    private UUID monthlyClosingId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public FinancialTransaction() {
    }

    public FinancialTransaction(Appointment appointment, UUID monthlyClosingId, String description, String category, BigDecimal amount, LocalDate transactionDate, TransactionType type, TransactionStatus status, String notes) {
        this.appointment = appointment;
        this.monthlyClosingId = monthlyClosingId;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.type = type;
        this.status = status;
        this.notes = notes;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public UUID getMonthlyClosingId() {
        return monthlyClosingId;
    }

    public void setMonthlyClosingId(UUID monthlyClosingId) {
        this.monthlyClosingId = monthlyClosingId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
