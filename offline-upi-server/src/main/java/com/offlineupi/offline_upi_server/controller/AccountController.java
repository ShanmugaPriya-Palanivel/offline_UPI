package com.offlineupi.offline_upi_server.controller;

import com.offlineupi.offline_upi_server.entity.Account;
import com.offlineupi.offline_upi_server.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountRepository.save(account);
    }
}