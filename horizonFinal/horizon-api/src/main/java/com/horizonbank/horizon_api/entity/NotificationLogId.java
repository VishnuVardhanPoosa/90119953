package com.horizonbank.horizon_api.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class NotificationLogId implements Serializable {

    private UUID eventId;
    private String accountNumber;

    public NotificationLogId() {
    }

    public NotificationLogId(UUID eventId, String accountNumber) {
        this.eventId = eventId;
        this.accountNumber = accountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationLogId)) return false;

        NotificationLogId that = (NotificationLogId) o;

        return Objects.equals(eventId, that.eventId)
                && Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, accountNumber);
    }
}