package com.example.demo.integration;


import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.service.currency.CurrencyServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
import com.example.demo.tool.TransferOperationTestDataCreator;
import com.example.demo.tool.UserTestDataCreator;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class TransferOperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestDataCreator userTestDataCreator;

    @Autowired
    private AccountTestDataCreator accountTestDataCreator;

    @Autowired
    private TransferOperationTestDataCreator operationTestDataCreator;

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
    public void processOperationWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.getSavedAccount(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

            mockMvc.perform(post("/transfer")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .with(httpBasic(user.getUsername(), user.getPassword()))
                .param("fromAccountId", operation.getFromAccountId().toString())
                .param("toAccountId", operation.getToAccountId().toString())
                .param("funds", operation.getFunds().toString())
                .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpect(status().isOk());

        Account updatedFromAccount = accountService.getById(fromAccount.getId());
        Account updatedToAccount = accountService.getById(toAccount.getId());

        assertEquals(operation.getFromAccountCurrency(), updatedFromAccount.getCurrency());
        assertEquals(operation.getFromAccountCurrency(), updatedToAccount.getCurrency());

        BigDecimal expectedAmountOfMoneyFromAccount = fromAccount.getFunds().subtract(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyFromAccount = updatedFromAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyFromAccount, actualAmountOfMoneyFromAccount);

        BigDecimal expectedAmountOfMoneyToAccount = toAccount.getFunds().add(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyToAccount = updatedToAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyToAccount, actualAmountOfMoneyToAccount);
    }



    @Test
    public void processOperationBetweenDifferentUsers() throws Exception {
        User fromUser = userTestDataCreator.getRegisteredUserById(1);
        User toUser = userTestDataCreator.getRegisteredUserById(2);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, fromUser.getId());
        Account toAccount = accountTestDataCreator.getSavedAccount(2, toUser.getId());
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, fromAccount.getId(), toAccount.getId(), fromUser.getId(), toUser.getId());

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(fromUser.getUsername(), fromUser.getPassword()))
                        .param("fromAccountId", operation.getFromAccountId().toString())
                        .param("toAccountId", operation.getToAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpect(status().isOk());

        Account updatedFromAccount = accountService.getById(fromAccount.getId());
        Account updatedToAccount = accountService.getById(toAccount.getId());

        assertEquals(operation.getFromAccountCurrency(), updatedFromAccount.getCurrency());
        assertEquals(operation.getFromAccountCurrency(), updatedToAccount.getCurrency());

        BigDecimal expectedAmountOfMoneyFromAccount = fromAccount.getFunds().subtract(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyFromAccount = updatedFromAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyFromAccount, actualAmountOfMoneyFromAccount);

        BigDecimal expectedAmountOfMoneyToAccount = toAccount.getFunds().add(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyToAccount = updatedToAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyToAccount, actualAmountOfMoneyToAccount);
    }

    @Test
    public void processOperationToSameAccount() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, account.getId(), account.getId(), user.getId(), user.getId());

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("fromAccountId", operation.getFromAccountId().toString())
                        .param("toAccountId", operation.getToAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    public void processOperationWithCorrectScenarioWithDifferentCurrency() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.generateAccount(2, user.getId());
        toAccount.setCurrency(
                Arrays.stream(Currency.values())
                        .filter(x -> x != fromAccount.getCurrency())
                        .findFirst()
                        .get()
        );
        accountTestDataCreator.saveAccount(toAccount);
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        operation.setFromAccountCurrency(fromAccount.getCurrency());

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("fromAccountId", operation.getFromAccountId().toString())
                        .param("toAccountId", operation.getToAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpect(status().isOk());


        Account updatedFromAccount = accountService.getById(fromAccount.getId());
        Account updatedToAccount = accountService.getById(toAccount.getId());
        BigDecimal exchangeRate = currencyService.getExchangeRate(
                updatedFromAccount.getCurrency(), updatedToAccount.getCurrency());
        BigDecimal transferFunds = operation.getFunds().multiply(exchangeRate);

        assertEquals(operation.getFromAccountCurrency(), updatedFromAccount.getCurrency());
        assertNotEquals(operation.getFromAccountCurrency(), updatedToAccount.getCurrency());

        BigDecimal expectedAmountOfMoneyFromAccount = fromAccount.getFunds().subtract(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyFromAccount = updatedFromAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyFromAccount, actualAmountOfMoneyFromAccount);

        BigDecimal expectedAmountOfMoneyToAccount = toAccount.getFunds().add(transferFunds).stripTrailingZeros();
        BigDecimal actualAmountOfMoneyToAccount = updatedToAccount.getFunds().stripTrailingZeros();
        assertEquals(expectedAmountOfMoneyToAccount, actualAmountOfMoneyToAccount);
    }


    @Test
    public void processOperationWithInvalidData() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.getSavedAccount(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("fromAccountId", operation.getFromAccountId().toString())
                        .param("toAccountId", BigDecimal.valueOf(-10).toString())
                        .param("funds", BigDecimal.valueOf(-10).toString())
                        .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("transfer/create-form.html"),
                        model().attributeHasFieldErrorCode("operation", "toAccountId", "Min"),
                        model().attributeHasFieldErrorCode("operation", "funds", "Min")
                );
    }

    @Test
    public void processOperationWithNonExistentUserId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.generateAccount(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generateOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("fromAccountId", operation.getFromAccountId().toString())
                        .param("toAccountId", operation.getToAccountId().toString())
                        .param("funds", operation.getFunds().toString())
                        .param("fromAccountCurrency", operation.getFromAccountCurrency().name())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getOperationsByCurrentUserWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.getSavedAccount(2, user.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSavedOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSavedOperation(
                2, toAccount.getId(), fromAccount.getId(), user.getId(), user.getId());

        mockMvc.perform(get("/transfer/current-user")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("/transfer/list.html"),
                model().attribute("operations", List.of(operationSecond, operationFirst))
        );
    }

    @Test
    public void getOperationByAccountIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account fromAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account toAccount = accountTestDataCreator.getSavedAccount(2, user.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSavedOperation(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSavedOperation(
                2, toAccount.getId(), fromAccount.getId(), user.getId(), user.getId());

        mockMvc.perform(get("/transfer/account-id/" + fromAccount.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("/transfer/list.html"),
                model().attribute("operations", List.of(operationFirst, operationSecond))
        );

        mockMvc.perform(get("/transfer/account-id/" + toAccount.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("/transfer/list.html"),
                model().attribute("operations", List.of(operationSecond, operationFirst))
        );
    }

    @Test
    public void getOperationByNonExistentAccountId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.generateAccount(1, user.getId());

        mockMvc.perform(get("/replenishment/account-id/" + account.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().is(403)
        );
    }

}
