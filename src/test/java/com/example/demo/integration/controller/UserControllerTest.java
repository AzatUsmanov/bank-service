package com.example.demo.integration.controller;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.tool.UserTestDataCreator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.SQLException;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class UserControllerTest {

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
    public void userCreationWithCorrectScenario() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userTestData = userTestDataCreator.generate(2);
        var requestBuilder = MockMvcRequestBuilders
                .post("/user/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s",
                            "authorities" : ["%s", "%s"] 
                        }
                        """,
                        userTestData.getUsername(),
                        userTestData.getEmail(),
                        userTestData.getPassword(),
                        userTestData.getUserAuthorities().getFirst().name(),
                        userTestData.getUserAuthorities().getLast().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated());
    }

    @Test
    public void userCreationWithoutNecessaryAuthorities() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userTestData = userTestDataCreator.generate(2);
        var requestBuilder = MockMvcRequestBuilders
                .post("/user/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s",
                            "authorities" : ["%s", "%s"] 
                        }
                        """,
                        userTestData.getUsername(),
                        userTestData.getEmail(),
                        userTestData.getPassword(),
                        userTestData.getUserAuthorities().getFirst().name(),
                        userTestData.getUserAuthorities().getLast().name()
                ));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }


    @Test
    public void userCreationWithNonUniqueUsername() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User registredUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        var requestBuilder = MockMvcRequestBuilders
                .post("/user/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : 1,
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s",
                            "authorities" : ["%s", "%s"]
                        }
                        """,
                        registredUser.getUsername(),
                        registredUser.getEmail() + "1",
                        registredUser.getPassword(),
                        registredUser.getUserAuthorities().getFirst().name(),
                        registredUser.getUserAuthorities().getLast().name()
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
    public void userCreationWithNonUniqueEmail() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User registredUser = userTestDataCreator.getRegisteredUserWithUserAuthoritiesById(2);
        var requestBuilder = MockMvcRequestBuilders
                .post("/user/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : 1,
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s",
                            "authorities" : ["%s", "%s"]
                        }
                        """,
                        registredUser.getUsername() + "1",
                        registredUser.getEmail(),
                        registredUser.getPassword(),
                        registredUser.getUserAuthorities().getFirst().name(),
                        registredUser.getUserAuthorities().getLast().name()
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
    public void userCreationWithInvalidData() throws Exception {
        User admin = userTestDataCreator.getRegisteredUserWithAdminAuthoritiesById(1);
        String token = userTestDataCreator.getAuthenticationToken(admin);
        User userWithInvalidData = User.builder()
                .id(2)
                .username("1")
                .email("1")
                .password("1")
                .authorities(List.of(Authority.USER_VIEW, Authority.USER_EDIT))
                .build();
        var requestBuilder = MockMvcRequestBuilders
                .post("/user/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "id" : 1,
                            "username" : "%s",
                            "email" : "%s",
                            "password" : "%s",
                            "authorities" : ["%s", "%s"]
                        }
                        """,
                        userWithInvalidData.getUsername(),
                        userWithInvalidData.getEmail(),
                        userWithInvalidData.getPassword(),
                        userWithInvalidData.getUserAuthorities().getFirst().name(),
                        userWithInvalidData.getUserAuthorities().getLast().name()
                ));;

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

}
