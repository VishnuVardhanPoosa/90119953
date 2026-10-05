package com.horizonbank.horizon_api.service;

import com.horizonbank.horizon_api.dto.TransferEvent;
import com.horizonbank.horizon_api.entity.NotificationLog;
// import com.horizonbank.horizon_api.entity.NotificationLogId;
import com.horizonbank.horizon_api.repository.NotificationLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import org.springframework.kafka.support.Acknowledgment;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class KafkaConsumerService {

    private final NotificationLogRepository notificationLogRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(
            NotificationLogRepository notificationLogRepository,
            ObjectMapper objectMapper) {

        this.notificationLogRepository = notificationLogRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "bank.transfer.completed.v1",
            groupId = "horizon-notification-group"
    )
    public void consumeTransferEvent(
        String message,
        Acknowledgment acknowledgment) {

        try {

            TransferEvent event =
                    objectMapper.readValue(message, TransferEvent.class);

            /*
             * We are not changing TransferEvent.
             * The reference number is unique for a transfer, so we
             * derive a deterministic UUID from it for notification_log.
             */
            UUID eventId = UUID.nameUUIDFromBytes(
                    event.getReferenceNo()
                            .getBytes(StandardCharsets.UTF_8)
            );

            String senderMessage =
                    "Transfer of " + event.getAmount()
                            + " " + event.getCurrency()
                            + " completed from account "
                            + event.getFromAccount()
                            + " to "
                            + event.getToAccount()
                            + ". Reference: "
                            + event.getReferenceNo();

            String receiverMessage =
                    "You received " + event.getAmount()
                            + " " + event.getCurrency()
                            + " from account "
                            + event.getFromAccount()
                            + ". Reference: "
                            + event.getReferenceNo();

            NotificationLog senderNotification =
                    new NotificationLog(
                            eventId,
                            event.getFromAccount(),
                            "TRANSFER_COMPLETED",
                            "SMS",
                            senderMessage
                    );

            NotificationLog receiverNotification =
                    new NotificationLog(
                            eventId,
                            event.getToAccount(),
                            "TRANSFER_COMPLETED",
                            "SMS",
                            receiverMessage
                    );

            /*
             * Save each notification only if it does not already exist.
             * The primary key is (event_id, account_number).
             */
            saveIfNotExists(senderNotification);
            saveIfNotExists(receiverNotification);

            acknowledgment.acknowledge();

            System.out.println(
                    "Processed successful transfer event: "
                            + event.getReferenceNo()
            );

        } catch (Exception e) {
        System.err.println("Failed to process transfer event: "+ e.getMessage());

    throw new RuntimeException(
        "Failed to process transfer event",
        e
    );
}
    }

private void saveIfNotExists(NotificationLog notification) {

    notificationLogRepository.insertIfNotExists(
            notification.getEventId(),
            notification.getAccountNumber(),
            notification.getEventType(),
            notification.getChannel(),
            notification.getMessage()
    );
}

    @KafkaListener(
            topics = "bank.account.opened.v1",
            groupId = "horizon-notification-group"
    )
    public void consumeAccountOpenedEvent(String message) {

        System.out.println(
                "Received account opened event: "
                        + message
        );
    }

    @KafkaListener(
            topics = "bank.transfer.failed.v1",
            groupId = "horizon-notification-group"
    )
    public void consumeTransferFailedEvent(String message) {

        System.out.println(
                "Received failed transfer event: "
                        + message
        );
    }
}