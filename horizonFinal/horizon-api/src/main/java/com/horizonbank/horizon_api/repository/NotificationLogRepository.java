package com.horizonbank.horizon_api.repository;

import com.horizonbank.horizon_api.entity.NotificationLog;
import com.horizonbank.horizon_api.entity.NotificationLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, NotificationLogId> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO notification_log
            (event_id, account_number, event_type, channel, message)
        VALUES
            (:eventId, :accountNumber, :eventType, :channel, :message)
        ON CONFLICT (event_id, account_number) DO NOTHING
        """, nativeQuery = true)
    int insertIfNotExists(
            @Param("eventId") UUID eventId,
            @Param("accountNumber") String accountNumber,
            @Param("eventType") String eventType,
            @Param("channel") String channel,
            @Param("message") String message
    );
}