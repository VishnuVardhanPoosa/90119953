package com.horizonbank.horizon_api.repository;

import com.horizonbank.horizon_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByCustomerCustomerIdAndAccountType(
            Long customerId,
            String accountType
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a
        FROM Account a
        WHERE a.accountId = :accountId
        """)
    Optional<Account> findByIdForUpdate(
            @Param("accountId") Long accountId
    );
}