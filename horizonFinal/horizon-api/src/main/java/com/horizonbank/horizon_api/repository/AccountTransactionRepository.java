package com.horizonbank.horizon_api.repository;

import com.horizonbank.horizon_api.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountTransactionRepository
        extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccountAccountIdOrderByTxnTimeDescTxnIdDesc(
            Long accountId
    );
}