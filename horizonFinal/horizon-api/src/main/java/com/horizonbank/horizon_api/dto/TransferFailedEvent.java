package com.horizonbank.horizon_api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransferFailedEvent {

    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private OffsetDateTime failedAt;


    public TransferFailedEvent() {
    }


    public TransferFailedEvent(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String currency,
            String reason,
            OffsetDateTime failedAt) {

        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.failedAt = failedAt;
    }


    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getFailedAt() {
        return failedAt;
    }
}