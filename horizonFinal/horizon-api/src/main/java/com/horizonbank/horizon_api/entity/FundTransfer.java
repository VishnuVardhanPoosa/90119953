package com.horizonbank.horizon_api.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fund_transfer")
public class FundTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transfer_id", nullable = false)
    private UUID transferId;

    @Generated(event = EventType.INSERT)
    @Column(
            name = "reference_no",
            nullable = false,
            unique = true,
            length = 20,
            insertable = false,
            updatable = false
    )
    private String referenceNo;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 64
    )
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "from_account_id",
            nullable = false
    )
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "to_account_id",
            nullable = false
    )
    private Account toAccount;

    @Column(
            name = "amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "remarks",
            length = 100
    )
    private String remarks;

    @Column(
            name = "status",
            nullable = false,
            length = 10
    )
    private String status;

    @Column(
            name = "failure_reason",
            length = 40
    )
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    public FundTransfer() {
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}