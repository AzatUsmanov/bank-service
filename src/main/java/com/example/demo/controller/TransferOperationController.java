package com.example.demo.controller;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
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
 * Контроллер, выполняющий работу c операциями переводов {@link TransferOperation}
 */
@Controller
@RequestMapping("/transfer")
public class TransferOperationController {

    private final OperationService<TransferOperation> operationService;

    private final CurrentUserService currentUserService;

    public TransferOperationController(@Qualifier("TransferOperationSecurityProxyServiceImpl")
                                       OperationService<TransferOperation> operationService,
                                       CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
        this.operationService = operationService;
    }

    @ModelAttribute("currencies")
    public List<Currency> currencies() {
        return Arrays.stream(Currency.values()).toList();
    }

    /**
     * Метод, возвращающий форму для создания операции перевода {@link TransferOperation}
     * @param accountId - идентификатор счета {@link Account} с которого идет перевод
     * @return {@link ModelAndView} - представление с формой для создания операции перевода
     */
    @GetMapping("/create-form")
    public ModelAndView getCreateForm(@RequestParam("accountId") Integer accountId) {
        var modelAndView = new ModelAndView("transfer/create-form.html");
        var operation = new TransferOperation();
        operation.setFromAccountId(accountId);
        modelAndView.getModel().put("operation", operation);
        return modelAndView;
    }

    /**
     * Метод, принимающий данные из формы и вызывающий логику обработки операции перевода {@link TransferOperation}
     * @param operation {@link TransferOperation} - данные об операции перевода из формы
     * @param bindingResult {@link BindingResult} - список ошибок
     * @return {@link ModelAndView} - представление основной страницы
     */
    @PostMapping
    public ModelAndView process(@ModelAttribute("operation") @Valid TransferOperation operation, BindingResult bindingResult)
            throws NotEnoughFundsInAccountException, TransferToNonExistentAccountException, TransferToSameAccountException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("transfer/create-form.html", HttpStatus.BAD_REQUEST);
        }
        operation.setFromUserId(currentUserService.getCurrentUserId());
        operationService.process(operation);
        return new ModelAndView("home.html");
    }

    /**
     * Метод, возвращающий список операций переводов {@link List<TransferOperation>} текущего пользователя
     * @return {@link ModelAndView} - представление, содержащее список операций
     */
    @GetMapping("/current-user")
    public ModelAndView getByUserId() {
        var modelAndView = new ModelAndView("/transfer/list.html");
        Integer currentUserId = currentUserService.getCurrentUserId();
        List<TransferOperation> operations = operationService.getByUserId(currentUserId);
        modelAndView.getModel().put("operations", operations);
        return modelAndView;
    }

    /**
     * Метод, возвращающий список операций переводов {@link List<TransferOperation>} счета с идентификатором равным auditId
     * @param accountId - идентификатор счета
     * @return {@link ModelAndView} - представление, содержащее список операций
     */
    @GetMapping("/account-id/{id}")
    public ModelAndView getByAccountId(@PathVariable("id") Integer accountId) {
        var modelAndView = new ModelAndView("/transfer/list.html");
        List<TransferOperation> operations = operationService.getByAccountId(accountId);
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
    "fromAccountId" : %d,
    "toAccountId" : %d,
    "fromAccountCurrency" : "USD"
}
 */
