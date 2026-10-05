// package com.horizonbank.horizon_api.controller;

// import com.horizonbank.horizon_api.dto.AccountResponse;
// import com.horizonbank.horizon_api.dto.OpenAccountRequest;
// import com.horizonbank.horizon_api.service.AccountService;
// import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import com.horizonbank.horizon_api.dto.StatementResponse;
// import java.util.List;

// @RestController
// @RequestMapping("/api/v1/accounts")
// public class AccountController {

//     private final AccountService accountService;

//     public AccountController(AccountService accountService) {
//         this.accountService = accountService;
//     }

//     @PostMapping
//     public ResponseEntity<AccountResponse> openAccount(
//             @Valid @RequestBody OpenAccountRequest request) {

//         AccountResponse response = accountService.openAccount(request);
//         return ResponseEntity
//                 .status(HttpStatus.CREATED)
//                 .body(response);
//     }

//     @GetMapping("/{accountNumber}")
//     public ResponseEntity<AccountResponse> getAccount(
//             @PathVariable String accountNumber) {

//         AccountResponse response = accountService.getAccount(accountNumber);
//         return ResponseEntity.ok(response);
//     }


//     @GetMapping("/{accountNumber}/statement")
//     ResponseEntity<List<StatementResponse>> getStatement(
//     @PathVariable String accountNumber) {
//         List<StatementResponse> response =
//             accountService.getStatement(accountNumber);

//     return ResponseEntity.ok(response);
// }
// }
package com.horizonbank.horizon_api.controller;

import com.horizonbank.horizon_api.dto.AccountCacheResult;
import com.horizonbank.horizon_api.dto.AccountResponse;
import com.horizonbank.horizon_api.dto.OpenAccountRequest;
import com.horizonbank.horizon_api.dto.StatementCacheResult;
import com.horizonbank.horizon_api.dto.StatementResponse;
import com.horizonbank.horizon_api.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(
            @Valid @RequestBody OpenAccountRequest request) {

        AccountResponse response =
                accountService.openAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {

        AccountCacheResult result =
        accountService.getAccount(accountNumber);

return ResponseEntity
        .ok()
        .header(
                "X-Cache",
                result.isCacheHit() ? "HIT" : "MISS"
        )
        .body(result.getAccount());
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<List<StatementResponse>> getStatement(
            @PathVariable String accountNumber) {

        StatementCacheResult result =
                accountService.getStatement(accountNumber);

        return ResponseEntity
                .ok()
                .header(
                        "X-Cache",
                        result.isCacheHit() ? "HIT" : "MISS"
                )
                .body(result.getStatements());
    }
}