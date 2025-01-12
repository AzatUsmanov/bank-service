package com.example.demo.integration;

import com.example.demo.domain.model.User;
import com.example.demo.tool.UserTestDataCreator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestDataCreator userTestDataCreator;

    @BeforeEach
    public void cleanDataBase() throws SQLException {
        userTestDataCreator.deleteAuthoritiesDataBaseData();
        userTestDataCreator.deleteUserDataBaseData();
    }

    @Test
    public void signUpWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.generateUser(1);
        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", user.getUsername())
                        .param("email", user.getEmail())
                        .param("password", user.getPassword())
                )
                .andExpect(status().isOk());
    }

    @Test
    public void signUpWithInvalidData() throws Exception {
        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "pa")
                        .param("email", "AzatUsmanov")
                        .param("password", "ps")
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("auth/sign-up.html"),
                        model().attributeHasFieldErrorCode("request", "username", "Size"),
                        model().attributeHasFieldErrorCode("request", "email", "Email"),
                        model().attributeHasFieldErrorCode("request", "password", "Size")
                );
    }

    @Test
    public void signUpWithNonUniqueUsername() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        User userForSignUp = userTestDataCreator.generateUser(2);
        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", user.getUsername())
                        .param("email", userForSignUp.getEmail())
                        .param("password", userForSignUp.getPassword())
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("auth/sign-up.html"),
                        model().attribute("globalError", "Not unique username")
                );
    }

    @Test
    public void signUpWithNonUniqueEmail() throws Exception {
        User user = userTestDataCreator.getRegisteredUserById(1);
        User userForSignUp = userTestDataCreator.generateUser(2);
        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", userForSignUp.getUsername())
                        .param("email", user.getEmail())
                        .param("password", userForSignUp.getPassword())
                )
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("auth/sign-up.html"),
                        model().attribute("globalError", "Not unique email")
                );
    }

}

