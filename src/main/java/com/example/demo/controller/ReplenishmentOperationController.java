package com.example.demo.controller;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.service.authentication.CurrentUserService;
import com.example.demo.service.operation.OperationService;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;

import com.example.demo.tool.exception.TransferToNonExistentAccountException;
import com.example.demo.tool.exception.TransferToSameAccountException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * Контроллер, выполняющий работу c операциями пополнения {@link ReplenishmentOperation}
 */
@Controller
@RequestMapping("/replenishment")
public class ReplenishmentOperationController {

    private final OperationService<ReplenishmentOperation> operationService;
    private final CurrentUserService currentUserService;

    public ReplenishmentOperationController(@Qualifier("ReplenishmentOperationSecurityProxyServiceImpl")
                                            OperationService<ReplenishmentOperation> operationService, CurrentUserService currentUserService) {
        this.operationService = operationService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currencies")
    public List<Currency> currencies() {
        return Arrays.stream(Currency.values()).toList();
    }

    /**
     * Метод, возвращающий форму для создания операции пополнения {@link ReplenishmentOperation}
     * @param accountId - идентификатор счета {@link Account} на который идет пополнение
     * @return {@link ModelAndView} - представление с формой для создания операции пополнения
     */
    @GetMapping("/create-form")
    public ModelAndView getCreateForm(@RequestParam("accountId") Integer accountId) {
        var modelAndView = new ModelAndView("replenishment/create-form.html");
        var operation = new ReplenishmentOperation();
        operation.setAccountId(accountId);
        modelAndView.getModel().put("operation", operation);
        return modelAndView;
    }

    /**
     * Метод, принимающий данные из формы и вызывающий логику обработки операции  {@link ReplenishmentOperation}
     * @param operation {@link ReplenishmentOperation} - данные об операции перевода из формы
     * @param bindingResult {@link BindingResult} - список ошибок
     * @return {@link ModelAndView} - представление основной страницы
     */
    @PostMapping
    public ModelAndView process(@ModelAttribute("operation") @Valid ReplenishmentOperation operation, BindingResult bindingResult)
            throws NotEnoughFundsInAccountException, TransferToNonExistentAccountException, TransferToSameAccountException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("replenishment/create-form.html", HttpStatus.BAD_REQUEST);
        }
        operation.setUserId(currentUserService.getCurrentUserId());
        operationService.process(operation);
        return new ModelAndView("home.html");
    }

    /**
     * Метод, возвращающий список операций переводов {@link List<ReplenishmentOperation>} текущего пользователя
     * @return {@link ModelAndView} - представление, содержащее список операций
     */
    @GetMapping("/current-user")
    public ModelAndView getByUserId() {
        var modelAndView = new ModelAndView("/replenishment/list.html");
        Integer currentUserId = currentUserService.getCurrentUserId();
        List<ReplenishmentOperation> operations = operationService.getByUserId(currentUserId);
        modelAndView.getModel().put("operations", operations);
        return modelAndView;
    }

    /**
     * Метод, возвращающий список операций переводов {@link List<ReplenishmentOperation>} счета с идентификатором равным auditId
     * @param accountId - идентификатор счета
     * @return {@link ModelAndView} - представление, содержащее список операций
     */
    @GetMapping("/account-id/{id}")
    public ModelAndView getByAccountId(@PathVariable("id") Integer accountId) {
        var modelAndView = new ModelAndView("/replenishment/list.html");
        List<ReplenishmentOperation> operations = operationService.getByAccountId(accountId);
        modelAndView.getModel().put("operations", operations);
        return modelAndView;
    }

}

/*
{
    "id" : 1,
    "userId" : 1,
    "dateOfCreation" : "2012-10-12",
    "funds" : 100.00,
    "accountId" : 2,
    "currency" : "USD"
}
 */