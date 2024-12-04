package com.example.demo.controller;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.domain.model.Authority.ADMIN_EDIT;

@RestController
@RequestMapping("/user")
@Tag(name = "UserController", description = "Контроллер с функцией создания пользователей")
public class UserController {

    private final UserService userService;

    public UserController(@Qualifier("UserSecurityProxyServiceImpl") UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            security = {@SecurityRequirement(name = "ADMIN_EDIT")},
            summary = "Запрос на создание пользователя.")
    public void create(@Valid @RequestBody User user) throws NotUniqueEmailException, NotUniqueUsernameException {
        userService.create(user);
    }

}
