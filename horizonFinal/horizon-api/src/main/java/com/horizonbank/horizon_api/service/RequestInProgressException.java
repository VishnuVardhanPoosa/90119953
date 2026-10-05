package com.horizonbank.horizon_api.service;

public class RequestInProgressException extends RuntimeException {

    public RequestInProgressException(String message) {
        super(message);
    }
}