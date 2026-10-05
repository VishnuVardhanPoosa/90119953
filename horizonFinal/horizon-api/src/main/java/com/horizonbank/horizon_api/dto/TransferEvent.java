package com.horizonbank.horizon_api.dto;

import java.math.BigDecimal;

public class TransferEvent {

    private String referenceNo;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;

    public TransferEvent() {
    }

    public TransferEvent(
            String referenceNo,
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String currency) {

        this.referenceNo = referenceNo;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}