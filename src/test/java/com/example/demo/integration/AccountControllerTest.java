package com.example.demo.integration;


import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountServiceImpl;
import com.example.demo.tool.AccountTestDataCreator;
import com.example.demo.tool.UserTestDataCreator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    public void createAccountWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.generateAccount(1, user.getId());

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                        .param("currency", account.getCurrency().name())
                )
                .andExpect(status().isCreated());
        Account savedAccount = accountService.getByUserId(user.getId()).getFirst();

        assertEquals(account.getCurrency(), savedAccount.getCurrency());
    }

    @Test
    public void deleteAccountWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());

        mockMvc.perform(post("/account/delete/" + account.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(httpBasic(user.getUsername(), user.getPassword()))
                )
                .andExpect(status().isOk());

        assertFalse(accountService.isPresentById(account.getId()));
    }


    @Test
    public void getAccountsByCurrentUserWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account firstAccount = accountTestDataCreator.getSavedAccount(1, user.getId());
        Account secondAccount = accountTestDataCreator.getSavedAccount(2, user.getId());

        mockMvc.perform(get("/account/current-user")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("account/list.html"),
                model().attribute("accounts", List.of(firstAccount, secondAccount))
        );
    }

    @Test
    public void getAccountByIdWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.getSavedAccount(1, user.getId());

        mockMvc.perform(get("/account/" + account.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().isOk(),
                view().name("account/one.html"),
                model().attribute("account", account)
        );
    }

    @Test
    public void getAccountByNonExistentId() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        Account account = accountTestDataCreator.generateAccount(1, user.getId());

        mockMvc.perform(get("/account/" + account.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(httpBasic(user.getUsername(), user.getPassword()))
        ).andExpectAll(
                status().is(403)
        );
    }


}

