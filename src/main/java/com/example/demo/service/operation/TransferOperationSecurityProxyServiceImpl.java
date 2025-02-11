package com.example.demo.service.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountService;
import com.example.demo.service.authentication.CurrentUserService;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;

import com.example.demo.tool.exception.TransferToNonExistentAccountException;
import com.example.demo.tool.exception.TransferToSameAccountException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Класс, реализующий логику проверки доступа к данным об операциях переводов между счетами {@link TransferOperation}
 */
@Service("TransferOperationSecurityProxyServiceImpl")
public class TransferOperationSecurityProxyServiceImpl implements OperationService<TransferOperation> {

    private final OperationService<TransferOperation> transferOperationOperationService;

    private final AccountService accountService;

    private final CurrentUserService currentUserService;

    public TransferOperationSecurityProxyServiceImpl(@Qualifier("TransferOperationServiceImpl")
                                                     OperationService<TransferOperation> transferOperationOperationService,
                                                    @Qualifier("AccountServiceImpl") AccountService accountService,
                                                     CurrentUserService currentUserService) {
        this.transferOperationOperationService = transferOperationOperationService;
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, проверяющий доступ пользователя к переводу средств на счет и вызывающий
     * метод, обрабатывающий операцию перевода между счетами {@link Account}
     * @param operation {@link TransferOperation} - информация о переводе средств между счетами
     */
    @Override
    public void process(TransferOperation operation) throws NotEnoughFundsInAccountException, TransferToNonExistentAccountException, TransferToSameAccountException {
        if (currentUserService.userHasNoAuthorityToEdit(operation.getFromUserId())) {
            throw new AccessDeniedException("Attempt to process an operation for another user");
        }
        transferOperationOperationService.process(operation);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях переводов {@link TransferOperation} между счетами {@link Account}
     * и вызывающий метод, возвращающий данные об операции перевода {@link TransferOperation} между счетами {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link TransferOperation} - данные об операции
     */
    @Override
    public TransferOperation getById(Integer id) {
        TransferOperation operation = transferOperationOperationService.getById(id);
        if (currentUserService.userHasNoAuthorityToView(operation.getFromUserId())
        && currentUserService.userHasNoAuthorityToView(operation.getToUserId())) {
            throw new AccessDeniedException("Attempt to get an operation by id for another user");
        }
        return operation;
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях переводов {@link TransferOperation} между счетами {@link Account}
     * и вызывающий метод, возвращающий информацию о всех операциях переводов {@link TransferOperation} между счетами {@link Account}
     * @param accountId - идентификатор счета, на информацию о пополнениях которого идет запрос
     * @return {@link List<TransferOperation>} - список операций переводов
     */
    @Override
    public List<TransferOperation> getByAccountId(Integer accountId) {
        if (!accountService.isPresentById(accountId)) {
            throw new AccessDeniedException("Attempt to get list of transfer operations by non-existent account id");
        }
        Account account = accountService.getById(accountId);
        if (currentUserService.userHasNoAuthorityToView(account.getUserId())) {
            throw new AccessDeniedException("Attempt to get an operations by account id for another user");
        }
        return transferOperationOperationService.getByAccountId(accountId);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях переводов {@link TransferOperation} между счетами {@link Account}
     * и вызывающий метод, возвращающий информацию о всех операциях переводов {@link TransferOperation}, совершенных пользователем {@link User}
     * с идентификатором равным userId
     * @param userId идентификатор пользователя, на информацию об операциях пополнения которого идет запрос
     * @return {@link List<TransferOperation>} - список операций переводов
     */
    @Override
    public List<TransferOperation> getByUserId(Integer userId) {
        if (currentUserService.userHasNoAuthorityToView(userId)) {
            throw new AccessDeniedException("Attempt to get an operations by user id for another user");
        }
        return transferOperationOperationService.getByUserId(userId);
    }

}
