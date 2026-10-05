package com.horizonbank.horizon_api.dto;

public class AccountCacheResult {

    private final AccountResponse account;
    private final boolean cacheHit;

    public AccountCacheResult(
            AccountResponse account,
            boolean cacheHit) {

        this.account = account;
        this.cacheHit = cacheHit;
    }

    public AccountResponse getAccount() {
        return account;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }
}