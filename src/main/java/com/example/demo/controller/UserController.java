package com.example.demo.controller;

import com.example.demo.service.authentication.CurrentUserService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Контроллер, выполняющий работу с данными пользовател
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, возвращающий данные о текущем пользователе
     * @return {@link ModelAndView} - представление, содержащее информацию о пользователе
     */
    @GetMapping
    public ModelAndView getUser() {
        var modelAndView = new ModelAndView("user/user.html");
        modelAndView.getModel().put("user", currentUserService.getCurrentUser());
        return modelAndView;
    }

}

/*
 {
    "id" : 1,
    "username": "username",
    "email": "mail@mail.com",
    "password": "password",
    "authorities": ["USER_VIEW", "USER_EDIT"]
 }

 */