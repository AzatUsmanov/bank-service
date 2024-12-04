package com.example.demo.controller;

import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.service.operation.OperationService;
import com.example.demo.tool.exception.NotEnoughFundsInAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/withdrawal")
@Tag(name = "WithdrawalOperationController", description = "Контроллер, обрабатывающий запросы на снятие средств со счета")
public class WithdrawalOperationController {

    private final OperationService<WithdrawalOperation> service;

    public WithdrawalOperationController(@Qualifier("WithdrawalOperationSecurityProxyServiceImpl")
                                         OperationService<WithdrawalOperation> service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            security = {@SecurityRequirement(name = "USER_EDIT"), @SecurityRequirement(name = "ADMIN_EDIT")},
            summary = "Запрос на снятие денег со счета")
    public void process(@Valid @RequestBody WithdrawalOperation operation) throws NotEnoughFundsInAccount {
        service.process(operation);
    }

    @GetMapping("/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_VIEW"), @SecurityRequirement(name = "ADMIN_VIEW")},
            summary = "Запрос на получения операции пополнения счета по id")
    public ResponseEntity<WithdrawalOperation> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getById(id));
    }

    @GetMapping("/account-id/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_VIEW"), @SecurityRequirement(name = "ADMIN_VIEW")},
            summary = "Запрос на получение всех операций снятия со счета")
    public ResponseEntity<List<WithdrawalOperation>> getByAccountId(@PathVariable("id") Integer accountId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getByAccountId(accountId));

    }

    @GetMapping("/user-id/{id}")
    @Operation(
            security = {@SecurityRequirement(name = "USER_VIEW"), @SecurityRequirement(name = "ADMIN_VIEW")},
            summary = "Запрос на получение всех операций снятия, совершенных пользователем")
    public ResponseEntity<List<WithdrawalOperation>> getByUserId(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getByUserId(userId));

    }

}
