package com.example.demo.integration.controller;


import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
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
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestDataCreator userTestDataCreator;

    @Autowired
    private AccountTestDataCreator accountTestDataCreator;

    @Autowired
    private AccountServiceImpl accountService;

    @BeforeEach
    public void cleanDataBase() throws SQLException {
        accountTestDataCreator.deleteAccountsData();
        userTestDataCreator.deleteAuthoritiesDataBaseData();
        userTestDataCreator.deleteUserDataBaseData();
    }

    @Test
    public void createAccountWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.generate(1, user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/account")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        account.getId(),
                        account.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(account.getDateOfCreation()),
                        account.getFunds().toString(),
                        account.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated());
    }

    @Test
    public void createAccountWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account account = accountTestDataCreator.generate(1, userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/account")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        account.getId(),
                        account.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(account.getDateOfCreation()),
                        account.getFunds().toString(),
                        account.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated());
    }

    @Test
    public void createAccountWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.generate(1, userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/account")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        account.getId(),
                        account.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(account.getDateOfCreation()),
                        account.getFunds().toString(),
                        account.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void createAccountWithInvalidData() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account accountWithInvalidData = Account.builder()
                .id(1)
                .funds(BigDecimal.valueOf(-12.12))
                .build();

        var requestBuilder = MockMvcRequestBuilders
                .post("/account")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : %s,
                            "funds" : %s,
                            "currency" : %s
                        }
                        """,
                        accountWithInvalidData.getId(),
                        accountWithInvalidData.getUserId(),
                        accountWithInvalidData.getDateOfCreation(),
                        accountWithInvalidData.getFunds(),
                        accountWithInvalidData.getCurrency()
                ));

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "funds": "funds in the account cannot be negative",
                            "currency": "currency is empty",
                            "userId": "account id is empty",
                            "dateOfCreation": "empty date of creation"
                        }
                        """)
                );
    }

    @Test
    public void createAccountWitNonExistentUserId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.generate(2);
        Account account = accountTestDataCreator.generate(1, userForAccountCreation.getId());

        var requestBuilder = MockMvcRequestBuilders
                .post("/account")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        account.getId(),
                        account.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(account.getDateOfCreation()),
                        account.getFunds().toString(),
                        account.getCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void updateAccountWithCorrectScenario() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account savedAccount = accountTestDataCreator.getSaved(1, admin.getId());
        Account testData = accountTestDataCreator.generate(2, admin.getId());

        var requestBuilder = MockMvcRequestBuilders
                .patch("/account/" + savedAccount.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        savedAccount.getId(),
                        savedAccount.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(testData.getDateOfCreation()),
                        testData.getFunds().toString(),
                        testData.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        Account updatedaccount = accountService.getById(savedAccount.getId());


        assertEquals(testData.getDateOfCreation().getTime(), updatedaccount.getDateOfCreation().getTime());
        assertEquals(testData.getFunds(), updatedaccount.getFunds());
        assertEquals(testData.getCurrency(), updatedaccount.getCurrency());
    }

    @Test
    public void updateAccountWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account testData = accountTestDataCreator.generate(2, user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .patch("/account/" + testData.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        testData.getId(),
                        testData.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(testData.getDateOfCreation()),
                        testData.getFunds().toString(),
                        testData.getCurrency().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }


    @Test
    public void updateAccountWithInvalidData() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account accountWithInvalidData = Account.builder()
                .id(1)
                .funds(BigDecimal.valueOf(-12.12))
                .build();
        var requestBuilder = MockMvcRequestBuilders
                .patch("/account/" + accountWithInvalidData.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : %s,
                            "funds" : %s,
                            "currency" : %s
                        }
                        """,
                        accountWithInvalidData.getId(),
                        accountWithInvalidData.getUserId(),
                        accountWithInvalidData.getDateOfCreation(),
                        accountWithInvalidData.getFunds(),
                        accountWithInvalidData.getCurrency()
                ));

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "funds": "funds in the account cannot be negative",
                            "currency": "currency is empty",
                            "userId": "account id is empty",
                            "dateOfCreation": "empty date of creation"
                        }
                        """)
                );
    }

    @Test
    public void updateAccountByNonExistentId() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        Account savedAccount = accountTestDataCreator.getSaved(1, admin.getId());
        Account testData = accountTestDataCreator.generate(2, admin.getId());

        var requestBuilder = MockMvcRequestBuilders
                .patch("/account/" + testData.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : %d,
                            "userId" : %d,
                            "dateOfCreation" : "%s",
                            "funds" : %s,
                            "currency" : "%s"
                        }
                        """,
                        savedAccount.getId(),
                        savedAccount.getUserId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(testData.getDateOfCreation()),
                        testData.getFunds().toString(),
                        testData.getCurrency().name()
                ));

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void deleteAccountWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .delete("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        assertThrows(IllegalArgumentException.class, () -> accountService.getById(account.getId()));
    }

    @Test
    public void deleteAccountWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .delete("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        assertThrows(IllegalArgumentException.class, () -> accountService.getById(account.getId()));
    }

    @Test
    public void deleteAccountWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userTestData = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userTestData.getId());

        var requestBuilder = MockMvcRequestBuilders
                .delete("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void deleteAccountByNonExistentId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.generate(1, user.getId());

        var requestBuilder = MockMvcRequestBuilders
                .delete("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);


        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getAccountByIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                        {
                                            "id" : %d,
                                            "userId" : %d,
                                            "dateOfCreation" : "%s",
                                            "funds" : %s,
                                            "currency" : %s
                                        }
                                        """,
                                account.getId(),
                                account.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(account.getDateOfCreation()),
                                account.getFunds(),
                                account.getCurrency()
                        ))
                );
    }

    @Test
    public void getAccountByIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                        {
                                            "id" : %d,
                                            "userId" : %d,
                                            "dateOfCreation" : "%s",
                                            "funds" : %s,
                                            "currency" : %s
                                        }
                                        """,
                                account.getId(),
                                account.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(account.getDateOfCreation()),
                                account.getFunds(),
                                account.getCurrency()
                        ))
                );
    }

    @Test
    public void getAccountByIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account account = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void getAccountsByNonExistentId() {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account account = accountTestDataCreator.generate(1, user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/" + account.getId())
                .header("Authorization", "Bearer " + token);;

        assertThrows(ServletException.class, () -> mockMvc.perform(requestBuilder));
    }

    @Test
    public void getAccountsByUserIdWithCorrectScenarioByUser() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        Account accountFirst = accountTestDataCreator.getSaved(1, user.getId());
        Account accountSecond = accountTestDataCreator.getSaved(2, user.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/user-id/" + user.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                        [
                                            {
                                                "id" : %d,
                                                "userId" : %d,
                                                "dateOfCreation" : "%s",
                                                "funds" : %s,
                                                "currency" : %s
                                            },
                                            {
                                                "id" : %d,
                                                "userId" : %d,
                                                "dateOfCreation" : "%s",
                                                "funds" : %s,
                                                "currency" : %s
                                            }
                                        ]
                                        """,
                                accountFirst.getId(),
                                accountFirst.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(accountFirst.getDateOfCreation()),
                                accountFirst.getFunds(),
                                accountFirst.getCurrency(),

                                accountSecond.getId(),
                                accountSecond.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(accountSecond.getDateOfCreation()),
                                accountSecond.getFunds(),
                                accountSecond.getCurrency()

                        ))
                );
    }

    @Test
    public void getAccountsByUserIdWithCorrectScenarioByAdmin() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account accountFirst = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account accountSecond = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/user-id/" + userForAccountCreation.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(String.format("""
                                        [
                                            {
                                                "id" : %d,
                                                "userId" : %d,
                                                "dateOfCreation" : "%s",
                                                "funds" : %s,
                                                "currency" : %s
                                            },
                                            {
                                                "id" : %d,
                                                "userId" : %d,
                                                "dateOfCreation" : "%s",
                                                "funds" : %s,
                                                "currency" : %s
                                            }
                                        ]
                                        """,
                                accountFirst.getId(),
                                accountFirst.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(accountFirst.getDateOfCreation()),
                                accountFirst.getFunds(),
                                accountFirst.getCurrency(),

                                accountSecond.getId(),
                                accountSecond.getUserId(),
                                new SimpleDateFormat("MMM dd, yyyy").format(accountSecond.getDateOfCreation()),
                                accountSecond.getFunds(),
                                accountSecond.getCurrency()

                        ))
                );
    }

    @Test
    public void getAccountsByUserIdWithoutNecessaryAuthorities() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(user);
        User userForAccountCreation = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        Account accountFirst = accountTestDataCreator.getSaved(1, userForAccountCreation.getId());
        Account accountSecond = accountTestDataCreator.getSaved(2, userForAccountCreation.getId());
        var requestBuilder = MockMvcRequestBuilders
                .get("/account/user-id/" + userForAccountCreation.getId())
                .header("Authorization", "Bearer " + token);;

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().is(403));
    }

}

