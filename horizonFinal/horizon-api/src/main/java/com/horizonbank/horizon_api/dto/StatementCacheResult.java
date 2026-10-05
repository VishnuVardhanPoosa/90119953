package com.horizonbank.horizon_api.dto;

import java.util.List;

public class StatementCacheResult {

    private List<StatementResponse> statements;
    private boolean cacheHit;

    public StatementCacheResult() {
    }

    public StatementCacheResult(
            List<StatementResponse> statements,
            boolean cacheHit) {

        this.statements = statements;
        this.cacheHit = cacheHit;
    }

    public List<StatementResponse> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementResponse> statements) {
        this.statements = statements;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit = cacheHit;
    }
}