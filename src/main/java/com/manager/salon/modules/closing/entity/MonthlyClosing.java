package com.manager.salon.modules.closing.entity;

import com.manager.salon.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_closings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_closing_year_month", columnNames = {"reference_year", "reference_month"})
})
public class MonthlyClosing extends BaseEntity {

    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    @Column(name = "gross_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossRevenue = BigDecimal.ZERO;

    @Column(name = "extra_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraRevenue = BigDecimal.ZERO;

    @Column(name = "total_expenses", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(name = "total_commissions", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissions = BigDecimal.ZERO;

    @Column(name = "net_result", nullable = false, precision = 12, scale = 2)
    private BigDecimal netResult = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MonthlyClosingStatus status = MonthlyClosingStatus.CLOSED;

    @Column(name = "closed_at", nullable = false)
    private OffsetDateTime closedAt;

    @Column(name = "closed_by", nullable = false, length = 150)
    private String closedBy;

    @Column(name = "audit_notes", columnDefinition = "TEXT")
    private String auditNotes;

    @OneToMany(mappedBy = "monthlyClosing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlyClosingItem> items = new ArrayList<>();

    public MonthlyClosing() {
    }

    public MonthlyClosing(Integer referenceYear, Integer referenceMonth, BigDecimal grossRevenue, BigDecimal extraRevenue, BigDecimal totalExpenses, BigDecimal totalCommissions, BigDecimal netResult, MonthlyClosingStatus status, OffsetDateTime closedAt, String closedBy, String auditNotes) {
        this.referenceYear = referenceYear;
        this.referenceMonth = referenceMonth;
        this.grossRevenue = grossRevenue;
        this.extraRevenue = extraRevenue;
        this.totalExpenses = totalExpenses;
        this.totalCommissions = totalCommissions;
        this.netResult = netResult;
        this.status = status;
        this.closedAt = closedAt;
        this.closedBy = closedBy;
        this.auditNotes = auditNotes;
    }

    public Integer getReferenceYear() {
        return referenceYear;
    }

    public void setReferenceYear(Integer referenceYear) {
        this.referenceYear = referenceYear;
    }

    public Integer getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(Integer referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(BigDecimal grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public BigDecimal getExtraRevenue() {
        return extraRevenue;
    }

    public void setExtraRevenue(BigDecimal extraRevenue) {
        this.extraRevenue = extraRevenue;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalCommissions() {
        return totalCommissions;
    }

    public void setTotalCommissions(BigDecimal totalCommissions) {
        this.totalCommissions = totalCommissions;
    }

    public BigDecimal getNetResult() {
        return netResult;
    }

    public void setNetResult(BigDecimal netResult) {
        this.netResult = netResult;
    }

    public MonthlyClosingStatus getStatus() {
        return status;
    }

    public void setStatus(MonthlyClosingStatus status) {
        this.status = status;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getAuditNotes() {
        return auditNotes;
    }

    public void setAuditNotes(String auditNotes) {
        this.auditNotes = auditNotes;
    }

    public List<MonthlyClosingItem> getItems() {
        return items;
    }

    public void setItems(List<MonthlyClosingItem> items) {
        this.items = items;
    }
}
