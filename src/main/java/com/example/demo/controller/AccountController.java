package com.example.demo.controller;

import com.example.demo.domain.dto.Account;
import com.example.demo.service.account.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account")
@Tag(name = "AccountController", description = "Контроллер с функциями CRUD для Account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(@Qualifier("AccountSecurityProxyServiceImpl") AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            security = {@SecurityRequirement(name = "USER_EDIT"), @SecurityRequirement(name = "ADMIN_EDIT")},
            summary = "Запрос на создание счета")
    public void create(@Valid @RequestBody Account account) {
        accountService.create(account);
    }

    @PatchMapping("/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "ADMIN_EDIT")},
            summary = "Запрос на обновление счета")
    public void updateById(@PathVariable("id") Integer id, @Valid @RequestBody Account account) {
        accountService.updateById(id, account);
    }

    @DeleteMapping("/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_EDIT"), @SecurityRequirement(name = "ADMIN_EDIT")},
            summary = "Запрос на удаление счета")
    public void deleteById(@PathVariable("id") Integer id) {
        accountService.deleteById(id);
    }

    @GetMapping("/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_VIEW"), @SecurityRequirement(name = "ADMIN_VIEW")},
            summary = "Запрос на получения счета по id")
    public ResponseEntity<Account> getById(@PathVariable("id") Integer accountId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.getById(accountId));
    }

    @GetMapping("/user-id/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_VIEW"), @SecurityRequirement(name = "ADMIN_VIEW")},
            summary = "Запрос на получения всех счетов пользователя")
    public ResponseEntity<List<Account>> getByUserId(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.getByUserId(userId));
    }

}

/*
{
    "id" : 1,
    "userId" : 1,
    "dateOfCreation" : "2012-12-31",
    "funds" : "12.12",
    "currency" : "USD"
}
 */