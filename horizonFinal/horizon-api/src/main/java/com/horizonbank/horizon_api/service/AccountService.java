

// package com.horizonbank.horizon_api.service;

// import com.horizonbank.horizon_api.dto.AccountResponse;
// import com.horizonbank.horizon_api.dto.OpenAccountRequest;
// import com.horizonbank.horizon_api.entity.Account;
// import com.horizonbank.horizon_api.entity.Customer;
// import com.horizonbank.horizon_api.repository.AccountRepository;
// import com.horizonbank.horizon_api.repository.CustomerRepository;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import com.horizonbank.horizon_api.entity.AccountTransaction;
// import com.horizonbank.horizon_api.repository.AccountTransactionRepository;
// import com.horizonbank.horizon_api.dto.StatementResponse;
// import java.util.List;
// import java.util.stream.Collectors;
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.OffsetDateTime;
// import java.time.Period;
// import java.util.Locale;
// import com.horizonbank.horizon_api.dto.AccountOpenedEvent;

// import org.springframework.data.redis.core.RedisTemplate;

// @Service
// public class AccountService {

//     private final CustomerRepository customerRepository;
//     private final AccountRepository accountRepository;
//     private final AccountTransactionRepository accountTransactionRepository;
//     private final RedisTemplate<String, AccountResponse> redisTemplate;
//     private final KafkaProducerService kafkaProducerService;

// public AccountService(
//         CustomerRepository customerRepository,
//         AccountRepository accountRepository,
//         AccountTransactionRepository accountTransactionRepository,
//         RedisTemplate<String, AccountResponse> redisTemplate,
//     KafkaProducerService kafkaProducerService) {

//     this.customerRepository = customerRepository;
//     this.accountRepository = accountRepository;
//     this.accountTransactionRepository = accountTransactionRepository;
//     this.redisTemplate = redisTemplate;
//     this.kafkaProducerService=kafkaProducerService;
// }

//     @Transactional
//     public AccountResponse openAccount(OpenAccountRequest request) {

//         String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
//         String pan = request.getPan().trim().toUpperCase(Locale.ROOT);
//         String phone = request.getPhone().trim();
//         String accountType = request.getAccountType().trim().toUpperCase(Locale.ROOT);

//         // Customer must be at least 18 years old
//         int age = Period.between(
//                 request.getDateOfBirth(),
//                 LocalDate.now()
//         ).getYears();

//         if (age < 18) {
//             throw new IllegalArgumentException(
//                     "Customer must be at least 18 years old"
//             );
//         }

//         // Validate account type
//         if (!accountType.equals("SAVINGS")
//                 && !accountType.equals("CURRENT")) {

//             throw new IllegalArgumentException(
//                     "Account type must be SAVINGS or CURRENT"
//             );
//         }

//         // Validate initial deposit
//         BigDecimal minimumDeposit;

//         if (accountType.equals("SAVINGS")) {
//             minimumDeposit = new BigDecimal("1000.00");
//         } else {
//             minimumDeposit = new BigDecimal("5000.00");
//         }

//         BigDecimal maximumDeposit = new BigDecimal("1000000.00");

//         if (request.getInitialDeposit().compareTo(minimumDeposit) < 0) {
//             throw new IllegalArgumentException(
//                     "Initial deposit for " + accountType
//                             + " must be at least " + minimumDeposit
//             );
//         }

//         if (request.getInitialDeposit().compareTo(maximumDeposit) > 0) {
//             throw new IllegalArgumentException(
//                     "Initial deposit cannot exceed 1000000.00"
//             );
//         }

//         // Find existing customer using PAN
//         Customer customer = customerRepository
//                 .findByPan(pan)
//                 .orElse(null);

//         if (customer == null) {

//             // Make sure email and phone are not already
//             // associated with another customer
//             if (customerRepository.findByEmail(email).isPresent()) {
//                 throw new IllegalArgumentException(
//                         "Customer with this email already exists"
//                 );
//             }

//             if (customerRepository.findByPhone(phone).isPresent()) {
//                 throw new IllegalArgumentException(
//                         "Customer with this phone already exists"
//                 );
//             }

//             // Create new customer
//             customer = new Customer();

//             customer.setFullName(request.getFullName().trim());
//             customer.setEmail(email);
//             customer.setPhone(phone);
//             customer.setDateOfBirth(request.getDateOfBirth());
//             customer.setPan(pan);
//             customer.setCreatedAt(OffsetDateTime.now());

//             customer = customerRepository.save(customer);

//         } else {

//             // Existing customer found.
//             // Make sure the supplied email and phone
//             // belong to this same customer.

//             if (!customer.getEmail().equals(email)) {
//                 throw new IllegalArgumentException(
//                         "PAN already belongs to a customer with a different email"
//                 );
//             }

//             if (!customer.getPhone().equals(phone)) {
//                 throw new IllegalArgumentException(
//                         "PAN already belongs to a customer with a different phone"
//                 );
//             }
//         }

//         // Check whether this customer already has
//         // an account of this type
//         if (accountRepository.existsByCustomerCustomerIdAndAccountType(
//                 customer.getCustomerId(),
//                 accountType)) {

//             throw new IllegalArgumentException(
//                     "Customer already has a " + accountType + " account"
//             );
//         }

//         // Create account
//         Account account = new Account();

//         account.setCustomer(customer);
//         account.setAccountType(accountType);
//         account.setCurrency("INR");
//         account.setBalance(request.getInitialDeposit());
//         account.setStatus("ACTIVE");
//         account.setVersion(0);
//         account.setOpenedAt(OffsetDateTime.now());
//         account.setUpdatedAt(OffsetDateTime.now());

//         account = accountRepository.save(account);

//         AccountOpenedEvent event =
//         new AccountOpenedEvent(
//                 account.getAccountNumber(),
//                 customer.getCustomerId(),
//                 customer.getFullName(),
//                 account.getAccountType(),
//                 account.getCurrency(),
//                 account.getBalance(),
//                 account.getOpenedAt()
//         );

// kafkaProducerService.sendAccountOpenedEvent(event);
//         // Create initial deposit ledger entry
//         AccountTransaction initialDeposit =new AccountTransaction();

//         initialDeposit.setAccount(account);
//         initialDeposit.setTransfer(null);
//         initialDeposit.setTxnType("CREDIT");
//         initialDeposit.setAmount(request.getInitialDeposit());
//         initialDeposit.setBalanceAfter(account.getBalance());
//         initialDeposit.setDescription("Initial deposit");
//         initialDeposit.setTxnTime(OffsetDateTime.now());

//         accountTransactionRepository.save(initialDeposit);

// // Build API response

        

//         // Build API response
//         AccountResponse response = new AccountResponse();

//         response.setAccountNumber(account.getAccountNumber());
//         response.setCustomerId(customer.getCustomerId());
//         response.setFullName(customer.getFullName());
//         response.setAccountType(account.getAccountType());
//         response.setCurrency(account.getCurrency());
//         response.setBalance(account.getBalance());
//         response.setStatus(account.getStatus());
//         response.setOpenedAt(account.getOpenedAt());

//         return response;
//     }

//     @Transactional(readOnly = true)
//     public AccountResponse getAccount(String accountNumber) {

//     String key = "horizon:account:" + accountNumber;

//     // Check Redis first
//     AccountResponse cachedAccount =
//             redisTemplate.opsForValue().get(key);

//     if (cachedAccount != null) {
//         System.out.println("redis hit");
//         return cachedAccount;
//     }

//     System.out.println("redis miss");
//     // Redis miss, get from PostgreSQL
//     Account account =
//             accountRepository
//                     .findByAccountNumber(accountNumber)
//                     .orElseThrow(() ->
//                             new IllegalArgumentException(
//                                     "Account not found: "
//                                             + accountNumber
//                             )
//                     );

//     Customer customer = account.getCustomer();

//     AccountResponse response = new AccountResponse();

//     response.setAccountNumber(account.getAccountNumber());
//     response.setCustomerId(customer.getCustomerId());
//     response.setFullName(customer.getFullName());
//     response.setAccountType(account.getAccountType());
//     response.setCurrency(account.getCurrency());
//     response.setBalance(account.getBalance());
//     response.setStatus(account.getStatus());
//     response.setOpenedAt(account.getOpenedAt());

//     // Store account in Redis
//     redisTemplate.opsForValue().set(key, response);

//     return response;
//     }


//     @Transactional(readOnly = true)
// public List<StatementResponse> getStatement(
//         String accountNumber) {

//     Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() ->
//                             new IllegalArgumentException("Account not found: "+ accountNumber));

//     List<AccountTransaction> transactions = accountTransactionRepository.findByAccountAccountIdOrderByTxnTimeDescTxnIdDesc(account.getAccountId());

//     return transactions.stream().map(transaction -> {

//                 StatementResponse response =
//                         new StatementResponse();

//                 response.setTxnId(
//                         transaction.getTxnId()
//                 );

//                 response.setTxnType(
//                         transaction.getTxnType()
//                 );

//                 response.setAmount(
//                         transaction.getAmount()
//                 );

//                 response.setBalanceAfter(
//                         transaction.getBalanceAfter()
//                 );

//                 response.setDescription(
//                         transaction.getDescription()
//                 );

//                 response.setTxnTime(
//                         transaction.getTxnTime()
//                 );

//                 return response;

//             })
//             .collect(Collectors.toList());
//         }
// }
package com.horizonbank.horizon_api.service;

import com.horizonbank.horizon_api.dto.AccountResponse;
import com.horizonbank.horizon_api.dto.OpenAccountRequest;
import com.horizonbank.horizon_api.dto.StatementCacheResult;
import com.horizonbank.horizon_api.dto.StatementResponse;
import com.horizonbank.horizon_api.entity.Account;
import com.horizonbank.horizon_api.entity.AccountTransaction;
import com.horizonbank.horizon_api.entity.Customer;
import com.horizonbank.horizon_api.repository.AccountRepository;
import com.horizonbank.horizon_api.repository.AccountTransactionRepository;
import com.horizonbank.horizon_api.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.horizonbank.horizon_api.dto.AccountCacheResult;
import com.horizonbank.horizon_api.dto.AccountOpenedEvent;
// import com.horizonbank.horizon_api.dto.AccountCacheResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    private final RedisTemplate<String, AccountResponse> redisTemplate;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private final KafkaProducerService kafkaProducerService;

    public AccountService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository,
            RedisTemplate<String, AccountResponse> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            KafkaProducerService kafkaProducerService) {

        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public AccountResponse openAccount(OpenAccountRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String pan = request.getPan().trim().toUpperCase(Locale.ROOT);
        String phone = request.getPhone().trim();
        String accountType = request.getAccountType().trim().toUpperCase(Locale.ROOT);

        // Customer must be at least 18 years old
        int age = Period.between(
                request.getDateOfBirth(),
                LocalDate.now()
        ).getYears();

        if (age < 18) {
            throw new IllegalArgumentException(
                    "Customer must be at least 18 years old"
            );
        }

        // Validate account type
        if (!accountType.equals("SAVINGS")
                && !accountType.equals("CURRENT")) {

            throw new IllegalArgumentException(
                    "Account type must be SAVINGS or CURRENT"
            );
        }

        // Validate initial deposit
        BigDecimal minimumDeposit;

        if (accountType.equals("SAVINGS")) {
            minimumDeposit = new BigDecimal("1000.00");
        } else {
            minimumDeposit = new BigDecimal("5000.00");
        }

        BigDecimal maximumDeposit = new BigDecimal("1000000.00");

        if (request.getInitialDeposit().compareTo(minimumDeposit) < 0) {
            throw new IllegalArgumentException(
                    "Initial deposit for " + accountType
                            + " must be at least " + minimumDeposit
            );
        }

        if (request.getInitialDeposit().compareTo(maximumDeposit) > 0) {
            throw new IllegalArgumentException(
                    "Initial deposit cannot exceed 1000000.00"
            );
        }

        // Find existing customer using PAN
        Customer customer = customerRepository
                .findByPan(pan)
                .orElse(null);

        if (customer == null) {

            // Make sure email and phone are not already
            // associated with another customer
            if (customerRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException(
                        "Customer with this email already exists"
                );
            }

            if (customerRepository.findByPhone(phone).isPresent()) {
                throw new IllegalArgumentException(
                        "Customer with this phone already exists"
                );
            }

            // Create new customer
            customer = new Customer();

            customer.setFullName(request.getFullName().trim());
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setDateOfBirth(request.getDateOfBirth());
            customer.setPan(pan);
            customer.setCreatedAt(OffsetDateTime.now());

            customer = customerRepository.save(customer);

        } else {

            // Existing customer found.
            // Make sure the supplied email and phone
            // belong to this same customer.

            if (!customer.getEmail().equals(email)) {
                throw new IllegalArgumentException(
                        "PAN already belongs to a customer with a different email"
                );
            }

            if (!customer.getPhone().equals(phone)) {
                throw new IllegalArgumentException(
                        "PAN already belongs to a customer with a different phone"
                );
            }
        }

        // Check whether this customer already has
        // an account of this type
        if (accountRepository.existsByCustomerCustomerIdAndAccountType(
                customer.getCustomerId(),
                accountType)) {

            throw new IllegalArgumentException(
                    "Customer already has a " + accountType + " account"
            );
        }

        // Create account
        Account account = new Account();

        account.setCustomer(customer);
        account.setAccountType(accountType);
        account.setCurrency("INR");
        account.setBalance(request.getInitialDeposit());
        account.setStatus("ACTIVE");
        account.setVersion(0);
        account.setOpenedAt(OffsetDateTime.now());
        account.setUpdatedAt(OffsetDateTime.now());

        account = accountRepository.save(account);

        AccountOpenedEvent event =
                new AccountOpenedEvent(
                        account.getAccountNumber(),
                        customer.getCustomerId(),
                        customer.getFullName(),
                        account.getAccountType(),
                        account.getCurrency(),
                        account.getBalance(),
                        account.getOpenedAt()
                );

        kafkaProducerService.sendAccountOpenedEvent(event);

        // Create initial deposit ledger entry
        AccountTransaction initialDeposit = new AccountTransaction();

        initialDeposit.setAccount(account);
        initialDeposit.setTransfer(null);
        initialDeposit.setTxnType("CREDIT");
        initialDeposit.setAmount(request.getInitialDeposit());
        initialDeposit.setBalanceAfter(account.getBalance());
        initialDeposit.setDescription("Initial deposit");
        initialDeposit.setTxnTime(OffsetDateTime.now());

        accountTransactionRepository.save(initialDeposit);

        // Build API response
        AccountResponse response = new AccountResponse();

        response.setAccountNumber(account.getAccountNumber());
        response.setCustomerId(customer.getCustomerId());
        response.setFullName(customer.getFullName());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setOpenedAt(account.getOpenedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public AccountCacheResult getAccount(String accountNumber) {

        String key = "horizon:account:" + accountNumber;

        // Check Redis first
        AccountResponse cachedAccount =
                redisTemplate.opsForValue().get(key);

        if (cachedAccount != null) {
            System.out.println("redis hit");
            return new AccountCacheResult(cachedAccount,true);
        }

        System.out.println("redis miss");

        // Redis miss, get from PostgreSQL
        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + accountNumber
                                )
                        );

        Customer customer = account.getCustomer();

        AccountResponse response = new AccountResponse();

        response.setAccountNumber(account.getAccountNumber());
        response.setCustomerId(customer.getCustomerId());
        response.setFullName(customer.getFullName());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setOpenedAt(account.getOpenedAt());

        // Store account in Redis
        redisTemplate.opsForValue().set(key, response);

        return new AccountCacheResult(
        response,
        false
);
    }

    @Transactional(readOnly = true)
    public StatementCacheResult getStatement(
            String accountNumber) {

        String versionKey =
                "bank:stmt-ver:" + accountNumber;

        String version = null;

        /*
         * Get the statement version from Redis.
         *
         * If the version does not exist, use version 0.
         */
        try {
            version =
                    stringRedisTemplate
                            .opsForValue()
                            .get(versionKey);

        } catch (Exception e) {
            System.out.println(
                    "Redis unavailable while reading statement version"
            );
        }

        if (version == null) {
            version = "0";
        }

        String statementKey =
                "bank:stmt:"
                        + accountNumber
                        + ":v"
                        + version
                        + ":all";

        /*
         * Check Redis for the statement.
         */
        try {

            String cachedJson =
                    stringRedisTemplate
                            .opsForValue()
                            .get(statementKey);

            if (cachedJson != null) {

                StatementResponse[] cachedArray =
                        objectMapper.readValue(
                                cachedJson,
                                StatementResponse[].class
                        );

                List<StatementResponse> cachedStatements =
                        Arrays.asList(cachedArray);

                System.out.println("statement redis hit");

                return new StatementCacheResult(
                        cachedStatements,
                        true
                );
            }

        } catch (Exception e) {

            /*
             * Redis failure must not make the banking API fail.
             * We simply continue to PostgreSQL.
             */
            System.out.println(
                    "Redis unavailable while reading statement"
            );
        }

        System.out.println("statement redis miss");

        /*
         * Redis miss, get from PostgreSQL.
         */
        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + accountNumber
                                )
                        );

        List<AccountTransaction> transactions =
                accountTransactionRepository
                        .findByAccountAccountIdOrderByTxnTimeDescTxnIdDesc(
                                account.getAccountId()
                        );

        List<StatementResponse> statements =
                transactions.stream()
                        .map(transaction -> {

                            StatementResponse response =
                                    new StatementResponse();

                            response.setTxnId(
                                    transaction.getTxnId()
                            );

                            response.setTxnType(
                                    transaction.getTxnType()
                            );

                            response.setAmount(
                                    transaction.getAmount()
                            );

                            response.setBalanceAfter(
                                    transaction.getBalanceAfter()
                            );

                            response.setDescription(
                                    transaction.getDescription()
                            );

                            response.setTxnTime(
                                    transaction.getTxnTime()
                            );

                            return response;

                        })
                        .collect(Collectors.toList());

        /*
         * Store the statement in Redis for 5 minutes.
         */
        try {

            String json =
                    objectMapper.writeValueAsString(
                            statements
                    );

            stringRedisTemplate
                    .opsForValue()
                    .set(
                            statementKey,
                            json,
                            Duration.ofSeconds(300)
                    );

        } catch (Exception e) {

            /*
             * Redis failure must not make the banking API fail.
             */
            System.out.println(
                    "Redis unavailable while storing statement"
            );
        }

        return new StatementCacheResult(
                statements,
                false
        );
    }
}