package com.example.demo.controller;

import com.example.demo.domain.dto.authentication.JwtAuthenticationResponse;
import com.example.demo.domain.dto.authentication.SignInRequest;
import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.service.authentication.AuthenticationService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthenticationController", description = "Контроллер с функциями аутентификации и авторизации")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Запрос на регистрацию")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) throws NotUniqueEmailException, NotUniqueUsernameException {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Запрос на аутентификацию")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

}

/*
{
    "username" : "username",
    "email" : "mail@mail.com",
    "password" : "password"
}
 */