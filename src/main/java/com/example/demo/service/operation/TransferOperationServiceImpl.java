package com.example.demo.service.operation;

import com.example.demo.dao.operation.OperationDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountService;
import com.example.demo.service.currency.CurrencyService;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;

import com.example.demo.tool.exception.TransferToNonExistentAccountException;
import com.example.demo.tool.exception.TransferToSameAccountException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Класс, выполняющий бизнес логику по работе с операциями переводов между счетами {@link TransferOperation}
 */
@Slf4j
@Service("TransferOperationServiceImpl")
public class TransferOperationServiceImpl implements OperationService<TransferOperation> {

    private static final int MINIMUM_AMOUNT_OF_FUNDS_IN_ACCOUNT = 0;

    private final OperationDao<TransferOperation> transferOperationDao;

    private final AccountService accountService;

    private final UserService userService;

    private final CurrencyService currencyService;

    public TransferOperationServiceImpl(OperationDao<TransferOperation> transferOperationDao,
                                        @Qualifier("AccountServiceImpl") AccountService accountService,
                                        @Qualifier("UserServiceImpl") UserService userService,
                                        CurrencyService currencyService) {
        this.transferOperationDao = transferOperationDao;
        this.accountService = accountService;
        this.userService = userService;
        this.currencyService = currencyService;
    }

    /**
     * Метод, обрабатывающий операцию перевода между счетами {@link Account}
     * @param operation {@link TransferOperation} - информация о переводе средств между счетами
     */
    @Override
    @Transactional
    public void process(TransferOperation operation) throws NotEnoughFundsInAccountException, TransferToNonExistentAccountException, TransferToSameAccountException {
        if (!accountService.isPresentById(operation.getToAccountId())) {
            throw new TransferToNonExistentAccountException();
        }

        Account fromAccount = accountService.getById(operation.getFromAccountId());
        Account toAccount = accountService.getById(operation.getToAccountId());
        operation.setToUserId(toAccount.getUserId());
        operation.setFromAccountCurrency(fromAccount.getCurrency());
        BigDecimal transferFunds = countTransferFunds(fromAccount.getCurrency(), toAccount.getCurrency(), operation.getFunds());

        operation.setDateOfCreation(new Date());
        operationValidityCheck(operation);
        withdrawFundsFromAccount(fromAccount, operation.getFunds());
        replenishFundsToAccount(toAccount, transferFunds);

        accountService.updateById(operation.getFromAccountId(), fromAccount);
        accountService.updateById(operation.getToAccountId(), toAccount);
        transferOperationDao.save(operation);

        log.info("Saved transfer operation {}", operation);
    }

    /**
     * Метод возвращающий данные об операции перевода {@link TransferOperation} между счетами {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link TransferOperation} - данные об операции
     */
    @Override
    public TransferOperation getById(Integer id) {
        return transferOperationDao.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt to get transfer operation by non-existent id"));
    }

    /**
     * Метод, возвращающий информацию о всех операциях переводов {@link TransferOperation} между счетами {@link Account}
     * @param accountId - идентификатор счета, на информацию о пополнениях которого идет запрос
     * @return {@link List<TransferOperation>} - список операций переводов
     */
    @Override
    public List<TransferOperation> getByAccountId(Integer accountId) {
        if (!accountService.isPresentById(accountId)) {
            throw new IllegalArgumentException("Attempt to get list of transfer operations by non-existent account id");
        } else return transferOperationDao.getAllByAccountId(accountId);

    }

    /**
     * Метод, возвращающий информацию о всех операциях переводов {@link TransferOperation}, совершенных пользователем {@link User}
     * с идентификатором равным userId
     * @param userId идентификатор пользователя, на информацию об операциях пополнения которого идет запрос
     * @return {@link List<TransferOperation>} - список операций переводов
     */
    @Override
    public List<TransferOperation> getByUserId(Integer userId) {
        if (!userService.isPresentById(userId)) {
            throw  new IllegalArgumentException("Attempt to get list of transfer operations by non-existent user id");
        } else return transferOperationDao.getAllByUserId(userId);
    }


    /**
     * Метод, рассчитывающий количество средств, которые нужно перевести на счет {@link Account}
     * @param fromAccountCurrency - валюта счета, с которого снимают средства
     * @param toAccountCurrency - валюта счета, на который переводят средства
     * @param funds - количество средств в валюте fromAccountCurrency
     * @return количество средств, которые нужно перевести, в валюте toAccountCurrency
     */
    private BigDecimal countTransferFunds(Currency fromAccountCurrency, Currency toAccountCurrency, BigDecimal funds) {
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromAccountCurrency, toAccountCurrency);
        return funds.multiply(exchangeRate);
    }

    /**
     * Метод, пополняющий средства на счет {@link Account}
     * @param account {@link Account} - счет, на который переводят средства
     * @param funds - количество средств, которые нужно положить на счет
     */
    private void replenishFundsToAccount(Account account, BigDecimal funds) {
        BigDecimal newAmountOfFunds = account.getFunds().add(funds);
        account.setFunds(newAmountOfFunds);
    }

    /**
     * Метод, снимающий средства со счета {@link Account}
     * @param account {@link Account} - счет, с которого снимают средства
     * @param funds - средства, которые нужно снять со счета
     * @throws NotEnoughFundsInAccountException - исключение, возникающее при попытке списать средства с пустого счета
     */
    private void withdrawFundsFromAccount(Account account, BigDecimal funds) throws NotEnoughFundsInAccountException {
        if (account.getFunds().compareTo(funds) < MINIMUM_AMOUNT_OF_FUNDS_IN_ACCOUNT) {
            throw new NotEnoughFundsInAccountException("Attempt to transfer off funds from an empty account");
        }
        BigDecimal newAmountOfFunds = account.getFunds().add(funds.negate());
        account.setFunds(newAmountOfFunds);
    }

    
    /**
     * Метод, проверяющий, чтобы идентификатор счета, с которого списывают средства, не был равен
     * идентификатору счета, на который переводят средства
     * @param operation {@link TransferOperation} - информация о переводе
     */
    private void operationValidityCheck(TransferOperation operation) throws TransferToSameAccountException {
        if (Objects.equals(operation.getFromAccountId(), operation.getToAccountId())) {
            throw new TransferToSameAccountException("Attempt to make a transition to the same account");
        }
    }

}
