package com.example.demo.controller;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.service.account.AccountService;

import com.example.demo.service.authentication.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * Контроллер, выполняющий работу со счетами {@link Account}
 */
@RestController
@RequestMapping("/account")
@Tag(name = "AccountController", description = "Контроллер с функциями CRUD для Account")
public class AccountController {

    private final AccountService accountService;

    private final CurrentUserService currentUserService;

    public AccountController(@Qualifier("AccountSecurityProxyServiceImpl") AccountService accountService, CurrentUserService currentUserService) {
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currencies")
    public List<Currency> currencies() {
        return Arrays.stream(Currency.values()).toList();
    }

    /**
     * Метод, возвращающий форму для создания счета {@link Account}
     * @return {@link ModelAndView} - представление с формой для создания счета
     */
    @GetMapping("/create-form")
    public ModelAndView getCreateForm() {
        var modelAndView = new ModelAndView("/account/create-form.html");
        modelAndView.getModel().put("account", new Account());
        return modelAndView;
    }

    /**
     * Метод, принимающий данные из формы и вызывающий логику создания счета {@link Account}
     * @param account {@link  Account} - данные счета из формы
     * @return {@link ModelAndView} - представление основной страницы
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelAndView create(@ModelAttribute("account") Account account) {
        account.setUserId(currentUserService.getCurrentUserId());
        accountService.save(account);
        return new ModelAndView("home.html");
    }

    /**
     * Метод, удаляющий счет {@link Account}
     * @param id - идентификатор счета для удаления
     * @return {@link ModelAndView} - представление основной страницы
     */
    @PostMapping("delete/{id}")
    public ModelAndView deleteById(@PathVariable("id") Integer id) {
        accountService.deleteById(id);
        return new ModelAndView("home.html");
    }

    /**
     * Метод, возвращающий список счетов {@link List<Account>} текущего пользователя
     * @return {@link ModelAndView} - представление, содержащее список счетов
     */
    @GetMapping("/current-user")
    public ModelAndView getByCurrentUser() {
        var modelAndView = new ModelAndView("account/list.html");
        Integer userId = currentUserService.getCurrentUserId();
        List<Account> accounts = accountService.getByUserId(userId);
        modelAndView.getModel().put("accounts", accounts);
        return modelAndView;
    }

    /**
     * Метод, возвращающий список счетов текущего пользователя
     * @param id - идентификатор счета
     * @return {@link ModelAndView} - представление, содержащее информацию о счете
     */
    @GetMapping("/{id}")
    public ModelAndView getById(@PathVariable("id") Integer id) {
        var modelAndView = new ModelAndView("account/one.html");
        Account account = accountService.getById(id);
        modelAndView.getModel().put("account", account);
        return modelAndView;
    }

}

/*
{
    "id" : 1,
    "userId" : 1,
    "dateOfCreation" : "2012-12-31",
    "funds" : "12.12",
    "currency" : "USD"
}
 */