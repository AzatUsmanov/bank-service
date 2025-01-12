package com.example.demo.controller;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.service.authentication.AuthenticationService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import com.example.demo.domain.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Контроллер, выполняющий регистрацию пользователя {@link User}
 */
@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Метод, возвращающий форму для регистрации пользователя {@link User}
     * @return {@link ModelAndView} - представление с формой регистрации пользователя
     */
    @GetMapping("/sign-up")
    public ModelAndView getSignUpForm() {
        var modelAndView = new ModelAndView("auth/sign-up.html");
        modelAndView.getModel().put("request", new SignUpRequest());
        return modelAndView;
    }

    /**
     * Метод, принимающий запрос на регистрацию пользователя {@link User}
     * @param request {@link SignUpRequest} - запрос на регистрацию
     * @param bindingResult {@link BindingResult} - список ошибок
     * @return {@link ModelAndView} - представление основной страницы
     * @throws NotUniqueEmailException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальной почтой
     * @throws NotUniqueUsernameException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальным именем
     */
    @PostMapping("/sign-up")
    public ModelAndView signUp(@ModelAttribute("request") @Valid SignUpRequest request, BindingResult bindingResult)
            throws NotUniqueEmailException, NotUniqueUsernameException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("auth/sign-up.html", HttpStatus.BAD_REQUEST);
        }
        authenticationService.signUp(request);
        return new ModelAndView("home.html");
    }

}

/*
{
    "username" : "username",
    "email" : "mail@mail.com",
    "password" : "password"
}
 */