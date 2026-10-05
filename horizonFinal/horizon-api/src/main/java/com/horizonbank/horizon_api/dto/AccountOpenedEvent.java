package com.horizonbank.horizon_api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AccountOpenedEvent {

    private String accountNumber;
    private Long customerId;
    private String fullName;
    private String accountType;
    private String currency;
    private BigDecimal initialDeposit;
    private OffsetDateTime openedAt;


    public AccountOpenedEvent() {
    }


    public AccountOpenedEvent(
            String accountNumber,
            Long customerId,
            String fullName,
            String accountType,
            String currency,
            BigDecimal initialDeposit,
            OffsetDateTime openedAt) {

        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.fullName = fullName;
        this.accountType = accountType;
        this.currency = currency;
        this.initialDeposit = initialDeposit;
        this.openedAt = openedAt;
    }


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }


    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(OffsetDateTime openedAt) {
        this.openedAt = openedAt;
    }
}