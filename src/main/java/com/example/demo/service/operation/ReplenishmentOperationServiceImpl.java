package com.example.demo.service.operation;

import com.example.demo.dao.operation.OperationDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.service.account.AccountService;
import com.example.demo.service.currency.CurrencyService;
import com.example.demo.service.user.UserService;
import com.example.demo.domain.model.User;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс, выполняющий бизнес логику по работе с операциями пополнения счета {@link ReplenishmentOperation}
 */
@Slf4j
@Service("ReplenishmentOperationServiceImpl")
public class ReplenishmentOperationServiceImpl implements OperationService<ReplenishmentOperation> {

    private final OperationDao<ReplenishmentOperation> replenishmentOperationDao;

    private final AccountService accountService;

    private final UserService userService;

    private final CurrencyService currencyService;

    public ReplenishmentOperationServiceImpl(OperationDao<ReplenishmentOperation> replenishmentOperationDao,
                                             @Qualifier("AccountServiceImpl") AccountService accountService,
                                             @Qualifier("UserServiceImpl") UserService userService,
                                             CurrencyService currencyService) {
        this.replenishmentOperationDao = replenishmentOperationDao;
        this.accountService = accountService;
        this.userService = userService;
        this.currencyService = currencyService;
    }

    /**
     * Метод, обрабатывающий операцию пополнения средств на счет {@link Account}
     * @param operation {@link ReplenishmentOperation} - информация о пополнении средств на счет
     */
    @Override
    @Transactional
    public void process(ReplenishmentOperation operation) {
        try {
            Account account = accountService.getById(operation.getAccountId());
            BigDecimal replenishmentFunds = countReplenishmentFunds(operation.getCurrency(), account.getCurrency(), operation.getFunds());

            replenishFundsToAccount(account, replenishmentFunds);

            accountService.updateById(operation.getAccountId(), account);
            replenishmentOperationDao.save(operation);

            log.info("Saved replenishment operation {}", operation);
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while processing the replenishment operation", e);
        }
    }


    /**
     * Метод возвращающий данные об операции пополнения {@link ReplenishmentOperation} средств на счет {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link ReplenishmentOperation} - данные об операции
     */
    @Override
    public ReplenishmentOperation getById(Integer id) {
        try {
            return replenishmentOperationDao.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Attempt to get replenishment operation by non-existent id"));
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while receiving a replenishment operation by id", e);
        }
    }

    /**
     * Метод, возвращающий информацию о всех операциях пополнения {@link ReplenishmentOperation} на счет {@link Account} с идентификатором равным auditId
     * @param accountId - идентификатор счета, на информацию о пополнениях которого идет запрос
     * @return {@link List<ReplenishmentOperation>} - список операций пополнения
     */
    @Override
    public List<ReplenishmentOperation> getByAccountId(Integer accountId) {
        try {
            if (accountService.isPresentById(accountId)) {
                return replenishmentOperationDao.getAllByAccountId(accountId);
            } else {
                throw new IllegalArgumentException("Attempt to get list of replenishment operations by non-existent account id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting the list of replenishment operations by account id", e);
        }
    }

    /**
     * Метод, возвращающий информацию о всех операциях пополнения {@link ReplenishmentOperation}, совершенных пользователем {@link User}
     * с идентификатором равным userId
     * @param userId идентификатор пользователя, на информацию об операциях пополнения которого идет запрос
     * @return {@link List<ReplenishmentOperation>} - список операций пополнения
     */
    @Override
    public List<ReplenishmentOperation> getByUserId(Integer userId) {
        try {
            if (userService.isPresentById(userId)) {
                return replenishmentOperationDao.getAllByUserId(userId);
            } else {
                throw new IllegalArgumentException("Attempt to get list of replenishment operations by non-existent user id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting the list of replenishment operations by user id", e);
        }
    }

    /**
     * Метод, выполняющий пополнение средств на счет {@link Account}
     * @param account {@link Account} - счет, на который нужно зачислить средства
     * @param funds - количество средств, которые нужно положить на счет
     */
    private void replenishFundsToAccount(Account account, BigDecimal funds) {
        BigDecimal newAmountOfFunds = account.getFunds().add(funds);
        account.setFunds(newAmountOfFunds);
    }

    /**
     * Метод, рассчитывающий количество средств, которые нужно зачислить на счет {@link Account}
     * @param operationCurrency {@link Currency} - валюта средств, которые нужно зачислить на счет
     * @param accountCurrency {@link Currency} - валюта счета, на который нужно зачислить средства
     * @param funds - количество средств в исходной волюте
     * @return количество средств, переведенные в валюту счета
     */
    private BigDecimal countReplenishmentFunds(Currency operationCurrency, Currency accountCurrency, BigDecimal funds) {
        BigDecimal exchangeRate = currencyService.getExchangeRate(operationCurrency, accountCurrency);
        return funds.multiply(exchangeRate);
    }

}

