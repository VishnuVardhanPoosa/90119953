package com.horizonbank.horizon_api.repository;

import com.horizonbank.horizon_api.entity.FundTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface FundTransferRepository
        extends JpaRepository<FundTransfer, UUID> {

    Optional<FundTransfer> findByReferenceNo(String referenceNo);

    Optional<FundTransfer> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT COALESCE(SUM(f.amount), 0)
            FROM FundTransfer f
            WHERE f.fromAccount.accountId = :accountId
              AND f.status = 'COMPLETED'
              AND f.createdAt >= :startTime
              AND f.createdAt < :endTime
            """)
    BigDecimal sumCompletedOutgoingTransfers(
            @Param("accountId") Long accountId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );
}