package com.example.demo.integration.controller;


import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.service.currency.CurrencyServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
import com.example.demo.tool.TransferOperationTestDataCreator;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public void processOperationByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.getSaved(1, user.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void processOperationByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void processOperationWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.generate(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.generate(2, userForAccountCreation.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void processOperationBetweenDifferentUsers() throws Exception {
        User fromUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(fromUser);
        User toUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, fromUser.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, toUser.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), fromUser.getId(), toUser.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
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
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, account.getId(), account.getId(), user.getId(), user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void processOperationWithOneCurrency() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.getSaved(1, user.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
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
    public void processOperationWithCorrectScenarioWithDifferentCurrency() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.generate(1, user.getId());
        Account toAccount = accountTestDataCreator.generate(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        fromAccount.setCurrency(Currency.USD);
        toAccount.setCurrency(Currency.EUR);
        operation.setFromAccountCurrency(fromAccount.getCurrency());
        accountTestDataCreator.save(fromAccount);
        accountTestDataCreator.save(toAccount);


        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
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
    public void processOperationWithWrongOperationCurrency() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.generate(1, user.getId());
        Account toAccount = accountTestDataCreator.generate(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        fromAccount.setCurrency(Currency.USD);
        toAccount.setCurrency(Currency.EUR);
        operation.setFromAccountCurrency(Currency.RUB);
        accountTestDataCreator.save(fromAccount);
        accountTestDataCreator.save(toAccount);


        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void processOperationWithInvalidData() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        TransferOperation operation = TransferOperation.builder()
                .id(1)
                .funds(BigDecimal.valueOf(-10))
                .build();

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : %s,
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        operation.getDateOfCreation(),
                        operation.getFunds(),
                        operation.getFromAccountCurrency()
                ));


        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                {
                                    "fromAccountCurrency": "account id is empty",
                                    "fromAccountId": "from account id is empty",
                                    "toAccountId": "to account id is empty",
                                    "funds": "funds in the account cannot be negative",
                                    "fromUserId": "from user id is empty",
                                    "toUserId": "to user id is empty",
                                    "dateOfCreation": "empty date of creation"
                                }
                                """
                        ));
    }

    @Test
    public void processOperationWithNonExistentUserId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.generate(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, admin.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, admin.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                        operation.getId(),
                        operation.getFromUserId(),
                        operation.getToUserId(),
                        operation.getFromAccountId(),
                        operation.getToAccountId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(operation.getDateOfCreation()),
                        operation.getFunds().toString(),
                        operation.getFromAccountCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationByIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.getSaved(1, user.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, user.getId());
        TransferOperation operation = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                                operation.getId(),
                                operation.getFromUserId(),
                                operation.getToUserId(),
                                operation.getFromAccountId(),
                                operation.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operation.getDateOfCreation()),
                                operation.getFunds().toString(),
                                operation.getFromAccountCurrency().name()
                        )));
    }

    @Test
    public void getOperationByIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operation = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                        {
                            "id" : %d,
                            "fromUserId" : %d,
                            "toUserId" : %d,
                            "fromAccountId" : %d,
                            "toAccountId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "fromAccountCurrency" : "%s"
                        }
                        """,
                                operation.getId(),
                                operation.getFromUserId(),
                                operation.getToUserId(),
                                operation.getFromAccountId(),
                                operation.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operation.getDateOfCreation()),
                                operation.getFunds().toString(),
                                operation.getFromAccountCurrency().name()
                        )));
    }

    @Test
    public void getOperationByIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operation = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getOperationByNonExistentId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.generate(1, user.getId());
        Account toAccount = accountTestDataCreator.generate(2, user.getId());
        TransferOperation operation = operationTestDataCreator.generate(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/" + operation.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationsByUserIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.getSaved(1, user.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, user.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSaved(
                2, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/user-id/" + user.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()
                        )));
    }

    @Test
    public void getOperationsByUserIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSaved(
                2, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/user-id/" + userForAccountCreation.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()
                        )));
    }

    @Test
    public void getOperationsByUserIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSaved(
                2, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/user-id/" + userForAccountCreation.getId())
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
                .get("/transfer/user-id/" + userTestData.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getOperationByAccountIdByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account fromAccount = accountTestDataCreator.getSaved(1, user.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, user.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSaved(
                2, fromAccount.getId(), toAccount.getId(), user.getId(), user.getId());

        var requestBuilderByFromAccount = MockMvcRequestBuilders
                .get("/transfer/account-id/" + fromAccount.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilderByFromAccount)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()
                        )));

        var requestBuilderToAccount = MockMvcRequestBuilders
                .get("/transfer/account-id/" + toAccount.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilderToAccount)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()))
                );
    }

    @Test
    public void getOperationByAccountIdByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account fromAccount = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account toAccount = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        TransferOperation operationFirst = operationTestDataCreator.getSaved(
                1, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());
        TransferOperation operationSecond = operationTestDataCreator.getSaved(
                2, fromAccount.getId(), toAccount.getId(), userForAccountCreation.getId(), userForAccountCreation.getId());

        var requestBuilderByFromAccount = MockMvcRequestBuilders
                .get("/transfer/account-id/" + fromAccount.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilderByFromAccount)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()
                        )));

        var requestBuilderToAccount = MockMvcRequestBuilders
                .get("/transfer/account-id/" + toAccount.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilderToAccount)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                [
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    },
                                    {
                                        "id" : %d,
                                        "fromUserId" : %d,
                                        "toUserId" : %d,
                                        "fromAccountId" : %d,
                                        "toAccountId" : %d,
                                        "dateOfCreation" : "%s",
                                        "funds" : %s,
                                        "fromAccountCurrency" : "%s"
                                    }
                                ]
                                """,
                                operationFirst.getId(),
                                operationFirst.getFromUserId(),
                                operationFirst.getToUserId(),
                                operationFirst.getFromAccountId(),
                                operationFirst.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationFirst.getDateOfCreation()),
                                operationFirst.getFunds().toString(),
                                operationFirst.getFromAccountCurrency().name(),

                                operationSecond.getId(),
                                operationSecond.getFromUserId(),
                                operationSecond.getToUserId(),
                                operationSecond.getFromAccountId(),
                                operationSecond.getToAccountId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(operationSecond.getDateOfCreation()),
                                operationSecond.getFunds().toString(),
                                operationSecond.getFromAccountCurrency().name()))
                );
    }

    @Test
    public void getOperationsByAccountIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userTestData = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userTestData.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getOperationsByNonExistentAccountId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account account = accountTestDataCreator.generate(1, admin.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/transfer/account-id/" + account.getId())
                .header("Authorization", "Bearer " + token);

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

}
