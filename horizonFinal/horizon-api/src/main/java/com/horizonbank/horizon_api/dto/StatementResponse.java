package com.horizonbank.horizon_api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class StatementResponse {

    private Long txnId;
    private String txnType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private OffsetDateTime txnTime;

    public StatementResponse() {
    }

    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(OffsetDateTime txnTime) {
        this.txnTime = txnTime;
    }
}