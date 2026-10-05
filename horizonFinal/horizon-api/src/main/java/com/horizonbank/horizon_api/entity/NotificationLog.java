package com.horizonbank.horizon_api.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification_log",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "pk_notification_log",
            columnNames = {"event_id", "account_number"}
        )
    }
)
@IdClass(NotificationLogId.class)
public class NotificationLog {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Id
    @Column(name = "account_number", length = 12, nullable = false)
    private String accountNumber;

    @Column(name = "event_type", length = 40, nullable = false)
    private String eventType;

    @Column(name = "channel", length = 10, nullable = false)
    private String channel = "SMS";

    @Column(name = "message", length = 300, nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public NotificationLog() {
    }

    public NotificationLog(
            UUID eventId,
            String accountNumber,
            String eventType,
            String channel,
            String message) {

        this.eventId = eventId;
        this.accountNumber = accountNumber;
        this.eventType = eventType;
        this.channel = channel;
        this.message = message;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}