


package com.horizonbank.horizon_api.service;

import com.horizonbank.horizon_api.dto.TransferRequest;
import com.horizonbank.horizon_api.dto.TransferResponse;
import com.horizonbank.horizon_api.entity.Account;
import com.horizonbank.horizon_api.entity.AccountTransaction;
import com.horizonbank.horizon_api.entity.FundTransfer;
import com.horizonbank.horizon_api.repository.AccountRepository;
import com.horizonbank.horizon_api.repository.AccountTransactionRepository;
import com.horizonbank.horizon_api.repository.FundTransferRepository;

import org.springframework.data.redis.core.RedisTemplate;
import com.horizonbank.horizon_api.dto.AccountResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

import com.horizonbank.horizon_api.dto.TransferEvent;

import com.horizonbank.horizon_api.dto.TransferFailedEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class TransferService {

private final AccountRepository accountRepository;
private final AccountTransactionRepository accountTransactionRepository;
private final FundTransferRepository fundTransferRepository;

private final RedisTemplate<String, AccountResponse> redisTemplate;
private final StringRedisTemplate stringRedisTemplate;

private final KafkaProducerService kafkaProducerService;

private final ObjectMapper objectMapper;

private final TransactionTemplate transactionTemplate;

        public TransferService(
        AccountRepository accountRepository,
        AccountTransactionRepository accountTransactionRepository,
        FundTransferRepository fundTransferRepository,
        RedisTemplate<String, AccountResponse> redisTemplate,
        KafkaProducerService kafkaProducerService,
        ObjectMapper objectMapper,
        StringRedisTemplate stringRedisTemplate,
        PlatformTransactionManager transactionManager) {

    this.accountRepository = accountRepository;
    this.accountTransactionRepository =
            accountTransactionRepository;
    this.fundTransferRepository =
            fundTransferRepository;
    this.redisTemplate = redisTemplate;
    this.kafkaProducerService = kafkaProducerService;
    this.objectMapper=objectMapper;
    this.stringRedisTemplate=stringRedisTemplate;
     this.transactionTemplate =
            new TransactionTemplate(transactionManager);
        }

public TransferResult transfer(
        TransferRequest request,
        String idempotencyKey) {

    // Validate idempotency key
    if (idempotencyKey == null
            || idempotencyKey.trim().isEmpty()) {

        throw new IllegalArgumentException(
                "Idempotency-Key is required"
        );
    }

    String cleanIdempotencyKey =
            idempotencyKey.trim();

    if (cleanIdempotencyKey.length() > 64) {
        throw new IllegalArgumentException(
                "Idempotency-Key must not exceed 64 characters"
        );
    }

    String redisKey =
            "bank:idem:" + cleanIdempotencyKey;

    Boolean claimed =
            stringRedisTemplate.opsForValue().setIfAbsent(
                    redisKey,
                    "PENDING",
                    Duration.ofSeconds(86400)
            );

    if (Boolean.FALSE.equals(claimed)) {

        String storedResponse =
                stringRedisTemplate.opsForValue()
                        .get(redisKey);

        if ("PENDING".equals(storedResponse)) {

            throw new RequestInProgressException(
                    "Request is already being processed"
            );
        }

        if (storedResponse != null) {

            TransferResponse response =
                    objectMapper.readValue(
                            storedResponse,
                            TransferResponse.class
                    );

            return new TransferResult(
                    response,
                    true
            );
        }
    }

    try {

        TransferResponse response =
                transactionTemplate.execute(status ->
                        doTransfer(
                                request,
                                cleanIdempotencyKey
                        )
                );

        if (response != null) {

                    try {
        // Remove cached account details
        String fromAccountKey =
                "horizon:account:" +
                        request.getFromAccount();

        String toAccountKey =
                "horizon:account:" +
                        request.getToAccount();

        redisTemplate.delete(fromAccountKey);
        redisTemplate.delete(toAccountKey);

        // Increment statement versions
        String fromStatementVersionKey =
                "bank:stmt-ver:" +
                        request.getFromAccount();

        String toStatementVersionKey =
                "bank:stmt-ver:" +
                        request.getToAccount();

        stringRedisTemplate.opsForValue().increment(fromStatementVersionKey);
        stringRedisTemplate.opsForValue().increment(toStatementVersionKey);

    } catch (Exception e) {
        // Redis failure must not fail an already committed transfer
        System.err.println("Redis post-commit operation failed: " + e.getMessage());
    }

     String responseJson =
                    objectMapper.writeValueAsString(response);

            stringRedisTemplate.opsForValue()
                    .set(
                            redisKey,
                            responseJson,
                            Duration.ofSeconds(86400)
                    );
    try {
    FundTransfer savedTransfer =
            fundTransferRepository.findByIdempotencyKey(cleanIdempotencyKey)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Transfer not found after commit"));

    TransferEvent event =
            new TransferEvent(
                    savedTransfer.getReferenceNo(),
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.getCurrency()
            );

    kafkaProducerService.sendTransferEvent(event);

} catch (Exception e) {
    System.err.println(
            "Kafka post-commit operation failed: " +
                    e.getMessage()
    );
}
           
        }

        return new TransferResult(
                response,
                false
        );

    } catch (RuntimeException e) {

        stringRedisTemplate.delete(redisKey);

        throw e;
    }
}

private TransferResponse doTransfer(
        TransferRequest request,
        String idempotencyKey) {

    // Check whether this transfer was already processed
    FundTransfer existingTransfer =
            fundTransferRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElse(null);

    if (existingTransfer != null) {
        return buildResponse(existingTransfer);
    }

    // Find sender account
    Account fromAccount =
            accountRepository
                    .findByAccountNumber(
                            request.getFromAccount()
                    )
                    .orElseThrow(() -> {
                        publishFailedTransfer(
                                request,
                                "Sender account not found"
                        );

                        return new IllegalArgumentException(
                                "Sender account not found"
                        );
                    });

    // Find receiver account
    Account toAccount =
            accountRepository
                    .findByAccountNumber(
                            request.getToAccount()
                    )
                    .orElseThrow(() -> {
                        publishFailedTransfer(
                                request,
                                "Receiver account not found"
                        );

                        return new IllegalArgumentException(
                                "Receiver account not found"
                        );
                    });

    // Sender and receiver must be different
    if (fromAccount.getAccountId()
            .equals(toAccount.getAccountId())) {

        publishFailedTransfer(
                request,
                "Sender and receiver accounts must be different"
        );

        throw new IllegalArgumentException(
                "Sender and receiver accounts must be different"
        );
    }

    // Currency must be INR
    if (!"INR".equalsIgnoreCase(
            request.getCurrency())) {

        publishFailedTransfer(
                request,
                "Only INR transfers are supported"
        );

        throw new IllegalArgumentException(
                "Only INR transfers are supported"
        );
    }

    // Amount must be at least 1.00
    if (request.getAmount() == null
            || request.getAmount()
                    .compareTo(new BigDecimal("1.00")) < 0) {

        publishFailedTransfer(
                request,
                "Amount should be at least 1"
        );

        throw new IllegalArgumentException(
                "Transfer amount must be at least one"
        );
    }

    // Amount can have at most 2 decimal places
    if (request.getAmount().scale() > 2) {

        publishFailedTransfer(
                request,
                "Amount should have only 2 decimal places"
        );

        throw new IllegalArgumentException(
                "Transfer amount can have at most 2 decimal places"
        );
    }

    // Maximum transfer amount
    if (request.getAmount()
            .compareTo(new BigDecimal("200000.00")) > 0) {

        publishFailedTransfer(
                request,
                "Max transfer is 200000.00"
        );

        throw new IllegalArgumentException(
                "Maximum transfer amount is 200000.00"
        );
    }

    // Sender must be ACTIVE
    // if (!"ACTIVE".equals(fromAccount.getStatus())) {
    //     throw new IllegalArgumentException(
    //             "Sender account is not ACTIVE"
    //     );
    // }

    // Receiver must be ACTIVE
    // if (!"ACTIVE".equals(toAccount.getStatus())) {
    //     throw new IllegalArgumentException(
    //             "Receiver account is not ACTIVE"
    //     );
    // }

    // Sender must have sufficient balance
    // if (fromAccount.getBalance().compareTo(
    //         request.getAmount()) < 0) {
    //
    //     throw new IllegalArgumentException(
    //             "Insufficient account balance"
    //     );
    // }

    // Lock both accounts in account ID order
    Long firstAccountId;
    Long secondAccountId;

    if (fromAccount.getAccountId()
            < toAccount.getAccountId()) {

        firstAccountId = fromAccount.getAccountId();
        secondAccountId = toAccount.getAccountId();

    } else {

        firstAccountId = toAccount.getAccountId();
        secondAccountId = fromAccount.getAccountId();
    }

    Account firstLockedAccount =
            accountRepository
                    .findByIdForUpdate(firstAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Account not found"
                            )
                    );

    Account secondLockedAccount =
            accountRepository
                    .findByIdForUpdate(secondAccountId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Account not found"
                            )
                    );

    // Use the locked account objects
    if (fromAccount.getAccountId()
            .equals(firstLockedAccount.getAccountId())) {

        fromAccount = firstLockedAccount;
        toAccount = secondLockedAccount;

    } else {

        fromAccount = secondLockedAccount;
        toAccount = firstLockedAccount;
    }

    // Sender must be ACTIVE
    if (!"ACTIVE".equals(fromAccount.getStatus())) {

        publishFailedTransfer(
                request,
                "Sender account is not ACTIVE"
        );

        throw new IllegalArgumentException(
                "Sender account is not ACTIVE"
        );
    }

    // Receiver must be ACTIVE
    if (!"ACTIVE".equals(toAccount.getStatus())) {

        publishFailedTransfer(
                request,
                "Receiver account is not ACTIVE"
        );

        throw new IllegalArgumentException(
                "Receiver account is not ACTIVE"
        );
    }

    // Check balance after acquiring the lock
    if (fromAccount.getBalance()
            .compareTo(request.getAmount()) < 0) {

        publishFailedTransfer(
                request,
                "Insufficient account balance"
        );

        throw new IllegalArgumentException(
                "Insufficient account balance"
        );
    }

    // Check today's transfer limit
    ZoneId indiaZone =
            ZoneId.of("Asia/Kolkata");

    LocalDate today =
            LocalDate.now(indiaZone);

    OffsetDateTime startTime =
            today.atStartOfDay(indiaZone)
                    .toOffsetDateTime();

    OffsetDateTime endTime =
            today.plusDays(1)
                    .atStartOfDay(indiaZone)
                    .toOffsetDateTime();

    BigDecimal todayOutgoing =
            fundTransferRepository
                    .sumCompletedOutgoingTransfers(
                            fromAccount.getAccountId(),
                            startTime,
                            endTime
                    );

    BigDecimal dailyLimit =
            new BigDecimal("500000.00");

    if (todayOutgoing
            .add(request.getAmount())
            .compareTo(dailyLimit) > 0) {

        publishFailedTransfer(
                request,
                "Daily outgoing transfer limit exceeded"
        );

        throw new IllegalArgumentException(
                "Daily outgoing transfer limit of 500000.00 exceeded"
        );
    }

    // Calculate new balances
    BigDecimal amount =
            request.getAmount();

    BigDecimal senderNewBalance =
            fromAccount.getBalance()
                    .subtract(amount);

    BigDecimal receiverNewBalance =
            toAccount.getBalance()
                    .add(amount);

    // Update account balances
    fromAccount.setBalance(senderNewBalance);
    fromAccount.setUpdatedAt(
            OffsetDateTime.now()
    );

    toAccount.setBalance(receiverNewBalance);
    toAccount.setUpdatedAt(
            OffsetDateTime.now()
    );

    // Create fund transfer
    FundTransfer transfer =
            new FundTransfer();

    transfer.setIdempotencyKey(idempotencyKey);
    transfer.setFromAccount(fromAccount);
    transfer.setToAccount(toAccount);
    transfer.setAmount(amount);
    transfer.setStatus("COMPLETED");
    transfer.setCreatedAt(
            OffsetDateTime.now()
    );

    FundTransfer savedTransfer =
            fundTransferRepository.save(transfer);

    // Create DEBIT ledger entry
    AccountTransaction debit =
            new AccountTransaction();

    debit.setAccount(fromAccount);
    debit.setTransfer(savedTransfer);
    debit.setTxnType("DEBIT");
    debit.setAmount(amount);
    debit.setBalanceAfter(senderNewBalance);
    debit.setDescription(
            "Fund transfer to "
                    + toAccount.getAccountNumber()
    );
    debit.setTxnTime(
            OffsetDateTime.now()
    );

    // Create CREDIT ledger entry
    AccountTransaction credit =
            new AccountTransaction();

    credit.setAccount(toAccount);
    credit.setTransfer(savedTransfer);
    credit.setTxnType("CREDIT");
    credit.setAmount(amount);
    credit.setBalanceAfter(receiverNewBalance);
    credit.setDescription(
            "Fund transfer from "
                    + fromAccount.getAccountNumber()
    );
    credit.setTxnTime(
            OffsetDateTime.now()
    );

    // Save ledger entries
    accountTransactionRepository.save(debit);
    accountTransactionRepository.save(credit);

    // Remove cached account details
//     String fromAccountKey =
//             "horizon:account:" +
//                     fromAccount.getAccountNumber();

//     String toAccountKey =
//             "horizon:account:" +
//                     toAccount.getAccountNumber();

//     redisTemplate.delete(fromAccountKey);
//     redisTemplate.delete(toAccountKey);

//     TransferEvent event =
//             new TransferEvent(
//                     savedTransfer.getReferenceNo(),
//                     fromAccount.getAccountNumber(),
//                     toAccount.getAccountNumber(),
//                     amount,
//                     "INR"
//             );

//     kafkaProducerService.sendTransferEvent(event);

    // Build response
    return buildResponse(savedTransfer);
}

    private void publishFailedTransfer(
        TransferRequest request,
        String reason) {

    TransferFailedEvent event =
            new TransferFailedEvent(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.getCurrency(),
                    reason,
                    OffsetDateTime.now()
            );

    kafkaProducerService.sendTransferFailedEvent(event);
    }


    public TransferResponse getTransfer(String referenceNo) {

    FundTransfer transfer =
            fundTransferRepository
                    .findByReferenceNo(referenceNo)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Transfer not found"
                            )
                    );

    return buildResponse(transfer);
    }


    private TransferResponse buildResponse(
            FundTransfer transfer) {

        TransferResponse response =
                new TransferResponse();

        response.setReferenceNo(
                transfer.getReferenceNo()
        );

        response.setFromAccount(
                transfer.getFromAccount()
                        .getAccountNumber()
        );

        response.setToAccount(
                transfer.getToAccount()
                        .getAccountNumber()
        );

        response.setAmount(
                transfer.getAmount()
        );

        response.setCurrency("INR");

        response.setStatus(
                transfer.getStatus()
        );

        response.setCreatedAt(
                transfer.getCreatedAt()
        );

        return response;
    }
}