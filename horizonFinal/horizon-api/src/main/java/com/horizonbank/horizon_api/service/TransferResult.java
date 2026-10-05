package com.horizonbank.horizon_api.service;

import com.horizonbank.horizon_api.dto.TransferResponse;

public class TransferResult {

    private final TransferResponse response;
    private final boolean replayed;

    public TransferResult(
            TransferResponse response,
            boolean replayed) {

        this.response = response;
        this.replayed = replayed;
    }

    public TransferResponse getResponse() {
        return response;
    }

    public boolean isReplayed() {
        return replayed;
    }
}