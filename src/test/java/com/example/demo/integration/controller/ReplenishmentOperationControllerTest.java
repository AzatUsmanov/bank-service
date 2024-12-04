package com.example.demo.integration.controller;


import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.service.currency.CurrencyServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
import com.example.demo.tool.ReplenishmentOperationTestDataCreator;
import com.example.demo.tool.UserTestDataCreator;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class ReplenishmentOperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestDataCreator userTestDataCreator;

    @Autowired
    private AccountTestDataCreator accountTestDataCreator;

    @Autowired
    private ReplenishmentOperationTestDataCreator operationTestDataCreator;

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
    public void processOperationWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getCurrency().name()
                        ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        Account updatedAccount = accountService.getById(account.getId());

        BigDecimal expectedAmountOfMoney = account.getFunds().add(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoney =updatedAccount.getFunds().stripTrailingZeros();
        assertEquals(account.getCurrency(), updatedAccount.getCurrency());
        assertEquals(expectedAmountOfMoney, actualAmountOfMoney);
    }

    @Test
    public void processOperationWithDifferentCurrencies() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.generate(1, user.getId());
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), user.getId());
        account.setCurrency(Currency.USD);
        operation.setCurrency(Currency.EUR);
        accountTestDataCreator.save(account);

        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());


        Account updatedAccount = accountService.getById(account.getId());
        BigDecimal exchangeRate = currencyService.getExchangeRate(
                operation.getCurrency(), updatedAccount.getCurrency());
        BigDecimal replenishmentFunds = operation.getFunds().multiply(exchangeRate);

        BigDecimal expectedAmountOfMoney = account.getFunds().add(replenishmentFunds).stripTrailingZeros();
        BigDecimal actualAmountOfMoney = updatedAccount.getFunds().stripTrailingZeros();
        assertEquals(account.getCurrency(), updatedAccount.getCurrency());
        assertEquals(expectedAmountOfMoney, actualAmountOfMoney);
    }

    @Test
    public void processOperationWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        Account updatedAccount = accountService.getById(account.getId());

        BigDecimal expectedAmountOfMoney = account.getFunds().add(operation.getFunds()).stripTrailingZeros();
        BigDecimal actualAmountOfMoney =updatedAccount.getFunds().stripTrailingZeros();
        assertEquals(account.getCurrency(), updatedAccount.getCurrency());
        assertEquals(expectedAmountOfMoney, actualAmountOfMoney);
    }

    @Test
    public void processOperationWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));}

    @Test
    public void processOperationWithInvalidData() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        ReplenishmentOperation operation = ReplenishmentOperation.builder()
                .id(1)
                .funds(BigDecimal.valueOf(-10))
                .build();
        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : %s,
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        operation.getDateOfCreation(),
                        operation.getFunds(),
                        operation.getCurrency()
                ));

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {
                                    "accountId": "account id is empty",
                                    "funds": "funds in the account cannot be negative",
                                    "currency": "account id is empty",
                                    "userId": "user id is empty",
                                    "dateOfCreation": "empty date of creation"
                                }
                                """
                        ));
    }

    @Test
    public void processOperationWithNonExistentUserId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account account = accountTestDataCreator.getSaved(1, admin.getId());
        User userTestData = userTestDataCreator.generate(2);
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), userTestData.getId());
        var requestBuilder = MockMvcRequestBuilders
                .post("/replenishment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getUserId(),
                        operation.getAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationByIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        ReplenishmentOperation operation = operationTestDataCreator.getSaved(1, account.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                                operation.getId(),
                                operation.getUserId(),
                                operation.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operation.getDateOfCreation()),
                                operation.getFunds().toString(),
                                operation.getCurrency()
                        )));
    }

    @Test
    public void getOperationByIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operation = operationTestDataCreator.getSaved(1, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "accountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                                operation.getId(),
                                operation.getUserId(),
                                operation.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operation.getDateOfCreation()),
                                operation.getFunds().toString(),
                                operation.getCurrency()
                        )));
    }

    @Test
    public void getOperationByIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operation = operationTestDataCreator.getSaved(1, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getOperationByNonExistentId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.generate(1, userForAccountCreation.getId());
        ReplenishmentOperation operation = operationTestDataCreator.generate(1, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationsByUserIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        ReplenishmentOperation operationFirst = operationTestDataCreator.getSaved(1, account.getId(), user.getId());
        ReplenishmentOperation operationSecond = operationTestDataCreator.getSaved(2, account.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/user-id/" + user.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getUserId(),
                                operationFirst.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getCurrency(),

                                operationSecond.getId(),
                                operationSecond.getUserId(),
                                operationSecond.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getCurrency()
                        )));
    }

    @Test
    public void getOperationsByUserIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operationFirst = operationTestDataCreator.getSaved(1, account.getId(), userForAccountCreation.getId());
        ReplenishmentOperation operationSecond = operationTestDataCreator.getSaved(2, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/user-id/" + userForAccountCreation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getUserId(),
                                operationFirst.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getCurrency(),

                                operationSecond.getId(),
                                operationSecond.getUserId(),
                                operationSecond.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getCurrency()
                        )));
    }

    @Test
    public void getOperationsByUserIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userTestData = userTestDataCreator.generate(2);
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/user-id/" + userTestData.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getOperationsByNonExistentUserId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userTestData = userTestDataCreator.generate(2);
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/user-id/" + userTestData.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationByAccountIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        ReplenishmentOperation operationFirst = operationTestDataCreator.getSaved(1, account.getId(), user.getId());
        ReplenishmentOperation operationSecond = operationTestDataCreator.getSaved(2, account.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getUserId(),
                                operationFirst.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getCurrency(),

                                operationSecond.getId(),
                                operationSecond.getUserId(),
                                operationSecond.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getCurrency()
                        )));
    }

    @Test
    public void getOperationByAccountIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        ReplenishmentOperation operationFirst = operationTestDataCreator.getSaved(1, account.getId(), userForAccountCreation.getId());
        ReplenishmentOperation operationSecond = operationTestDataCreator.getSaved(2, account.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "userId" : %d,
                                        "accountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "currency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getUserId(),
                                operationFirst.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getCurrency(),

                                operationSecond.getId(),
                                operationSecond.getUserId(),
                                operationSecond.getAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getCurrency()
                        )));
    }

    @Test
    public void getOperationByAccountIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getOperationByNonExistentAccountId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userTestData = userTestDataCreator.generate(2);
        Account account = accountTestDataCreator.generate(1, userTestData.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/replenishment/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }
}

