package com.example.demo.integration;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.service.currency.CurrencyServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
import com.example.demo.tool.UserTestDataCreator;
import com.example.demo.tool.WithdrawalOperationTestDataCreator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class WithdrawalOperationControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestDataCreator userTestDataCreator;

    @Autowired
    private AccountTestDataCreator accountTestDataCreator;

    @Autowired
    private WithdrawalOperationTestDataCreator operationTestDataCreator;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private CurrencyServiceImpl currencyService;

    @BeforeEach
    public void cleanTestData() throws SQLException {
        operationTestDataCreator.deleteDataBaseData();
        accountTestDataCreator.deleteAccountsData();
        userTestDataCreator.deleteUserDataBaseData();
    }

    @Test
    public void processOperationWithCorrectScenarior() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operation = operationTestDataCreator.generateOperation(
                1, account.getId(), user.getId());

        mockMvc.perform(post("/withdrawal")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("accountId", operation.getAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("currency", operation.getCurrency().name())
                )
                .andExpect(status().isOk());


        Account updatedAccount = accountService.getById(account.getId());

        BigDecimal expectedAmountOfMoney = account.getFunds().subtract(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoney =updatedAccount.getFunds().stripTrailingZeros();
        assertEquals(account.getCurrency(), updatedAccount.getCurrency());
        assertEquals(expectedAmountOfMoney, actualAmountOfMoney);
    }

    @Test
    public void processOperationWithDifferentCurrencies() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operation = operationTestDataCreator.generateOperation(
                1, account.getId(), user.getId());
        operation.setCurrency(
                Arrays.stream(Currency.values())
                        .filter(x -> x != account.getCurrency())
                        .findFirst()
                        .get());

        mockMvc.perform(post("/withdrawal")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("accountId", operation.getAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("currency", operation.getCurrency().name())
                )
                .andExpect(status().isOk());

        Account updatedAccount = accountService.getById(account.getId());
        BigDecimal exchangeRate = currencyService.getExchangeRate(
                operation.getCurrency(), updatedAccount.getCurrency());
        BigDecimal withdrawalFunds = operation.getFunds().multiply(exchangeRate);

        BigDecimal expectedAmountOfMoney = account.getFunds().subtract(withdrawalFunds).stripTrailingZeros();
        BigDecimal actualAmountOfMoney = updatedAccount.getFunds().stripTrailingZeros();
        assertEquals(account.getCurrency(), updatedAccount.getCurrency());
        assertEquals(expectedAmountOfMoney, actualAmountOfMoney);
    }

    @Test
    public void processOperationWithInvalidData() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operation = operationTestDataCreator.generateOperation(
                1, account.getId(), user.getId());

        mockMvc.perform(post("/withdrawal")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("accountId", operation.getAccountId().toString())
                        .param("funds", BigDecimal.valueOf(-10).toString())
                        .param("currency", operation.getCurrency().name())
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("withdrawal/create-form.html"),
                        model().attributeHasFieldErrorCode("operation", "funds", "Min")
                );
    }

    @Test
    public void processOperationWithNotEnoughFundsInAccount() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operation = operationTestDataCreator.generateOperation(
                1, account.getId(), user.getId());
        operation.setFunds(operation.getFunds().add(account.getFunds()));

        mockMvc.perform(post("/withdrawal")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("accountId", operation.getAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("currency", operation.getCurrency().name())
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("withdrawal/create-form.html"),
                        model().attribute("globalError", "Not enough funds in account")
                );
    }

    @Test
    public void getOperationsByCurrentUserWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operationFirst = operationTestDataCreator.getSavedOperation(1, account.getId(), user.getId());
        WithdrawalOperation operationSecond = operationTestDataCreator.getSavedOperation(2, account.getId(), user.getId());

        mockMvc.perform(get("/withdrawal/current-user")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("/withdrawal/list.html"),
                model().attribute("operations", List.of(operationFirst, operationSecond))
        );
    }

    @Test
    public void getOperationByAccountIdWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        WithdrawalOperation operationFirst = operationTestDataCreator.getSavedOperation(1, account.getId(), user.getId());
        WithdrawalOperation operationSecond = operationTestDataCreator.getSavedOperation(2, account.getId(), user.getId());

        mockMvc.perform(get("/withdrawal/account-id/" + account.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("/withdrawal/list.html"),
                model().attribute("operations", List.of(operationFirst, operationSecond))
        );
    }

    @Test
    public void getOperationByNonExistentAccountId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.generateAccount(1, user.getId());

        mockMvc.perform(get("/withdrawal/account-id/" + account.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().is(403)
        );
    }

}
