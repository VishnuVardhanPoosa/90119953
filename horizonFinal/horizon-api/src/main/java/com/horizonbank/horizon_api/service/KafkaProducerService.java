package com.horizonbank.horizon_api.service;

import com.horizonbank.horizon_api.dto.TransferEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.horizonbank.horizon_api.dto.AccountOpenedEvent;
import com.horizonbank.horizon_api.dto.TransferFailedEvent;

@Service
public class KafkaProducerService {

private static final String TRANSFER_TOPIC =
        "bank.transfer.completed.v1";

private static final String ACCOUNT_TOPIC =
        "bank.account.opened.v1";

private static final String FAILED_TOPIC =
        "bank.transfer.failed.v1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendTransferEvent(
            TransferEvent event) {

        try {

            String json =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    TRANSFER_TOPIC,
                    json
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Kafka event",
                    e
            );
        }
    }

    public void sendAccountOpenedEvent(
        AccountOpenedEvent event) {

    try {

        String json =
                objectMapper.writeValueAsString(event);

        kafkaTemplate.send(
                ACCOUNT_TOPIC,
                json
        );

    } catch (Exception e) {

        throw new RuntimeException(
                "Failed to create account opened event",
                e
        );
    }
    }


    
    public void sendTransferFailedEvent(
        TransferFailedEvent event) {

    try {

        String json =
                objectMapper.writeValueAsString(event);

        kafkaTemplate.send(
                FAILED_TOPIC,
                json
        );

    } catch (Exception e) {

        throw new RuntimeException(
                "Failed to create transfer failed event",
                e
        );
    }
    }
}