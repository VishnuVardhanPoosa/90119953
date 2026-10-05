package com.horizonbank.horizon_api.controller;

import com.horizonbank.horizon_api.dto.TransferRequest;
import com.horizonbank.horizon_api.dto.TransferResponse;
import com.horizonbank.horizon_api.service.TransferResult;
import com.horizonbank.horizon_api.service.TransferService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

@PostMapping
public ResponseEntity<TransferResponse> transfer(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestHeader(value = "X-Correlation-Id", required = false)
                String correlationId,
        @RequestBody TransferRequest request) {

            if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
}
    TransferResult result =
            transferService.transfer(
                    request,
                    idempotencyKey
            );

        ResponseEntity.BodyBuilder response =
        ResponseEntity
                .status(HttpStatus.CREATED)
                .header("X-Correlation-Id", correlationId);

    if (result.isReplayed()) {
        response.header(
                "Idempotent-Replayed",
                "true"
        );
    }

    return response.body(result.getResponse());
}

    @GetMapping("/{referenceNo}")
    public ResponseEntity<TransferResponse> getTransfer(
        @PathVariable String referenceNo) {

            TransferResponse response =
            transferService.getTransfer(referenceNo);

        return ResponseEntity.ok(response);
    }
}