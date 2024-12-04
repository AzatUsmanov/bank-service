package com.example.demo.integration.controller;

import com.example.demo.domain.dto.authentication.JwtAuthenticationResponse;
import com.example.demo.domain.dto.authentication.SignInRequest;
import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.domain.model.User;
import com.example.demo.service.authentication.AuthenticationService;
import com.example.demo.tool.UserTestDataCreator;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username" : "username",
                            "email" : "mail@mail.com",
                            "password" : "password"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void signUpWithInvalidData() throws Exception {
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username" : "",
                            "email" : "dadas",
                            "password" : ""
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "password": "The length of the name must be between 5 and 20",
                            "email": "The mail format is not respected",
                            "username": "The length of the name must be between 5 and 20"
                        }
                        """)
                );
    }

    @Test
    public void signUpWithNonUniqueUsername() throws Exception {
        User regsteredUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        User userTestData = userTestDataCreator.generate(2);
        userTestData.setUsername(regsteredUser.getUsername());
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s"
                        }
                        """,
                        userTestData.getUsername(),
                        userTestData.getEmail(),
                        userTestData.getPassword()
                ));


        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "Error message" : "Received non-unique username for registration"
                        }
                        """)
                );
    }

    @Test
    public void signUpWithNonUniqueEmail() throws Exception {
        User regsteredUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        User userTestData = userTestDataCreator.generate(2);
        userTestData.setEmail(regsteredUser.getEmail());
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s"
                        }
                        """,
                        userTestData.getUsername(),
                        userTestData.getEmail(),
                        userTestData.getPassword()
                ));


        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "Error message" : "Received non-unique email for registration"
                        }
                        """)
                );
    }

    @Test
    public void signInWithCorrectScenario() throws Exception {
        User user = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "username" : "%s",
                            "password" : "%s"
                        }
                        """,
                        user.getUsername(),
                        user.getPassword()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void signInWithInvalidData() throws Exception {
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username" : "",
                            "password" : ""
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                            "password": "The length of the name must be between 5 and 20",
                            "username": "The length of the name must be between 5 and 20"
                        }
                        """)
                );
    }

    @Test
    public void signInWithoutRegistering() throws Exception {
        var requestBuilder = MockMvcRequestBuilders
                .post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username" : "username",
                            "password" : "password"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

}

