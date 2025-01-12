package com.example.demo.service.operation;

import com.example.demo.dao.operation.OperationDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountService;
import com.example.demo.service.currency.CurrencyService;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Класс, выполняющий бизнес логику по работе с операциями снятия средств со счета {@link WithdrawalOperation}
 */
@Slf4j
@Service("WithdrawalOperationServiceImpl")
public class WithdrawalOperationServiceImpl implements OperationService<WithdrawalOperation> {

    private static final int MINIMUM_AMOUNT_OF_FUNDS_IN_ACCOUNT = 0;

    private final OperationDao<WithdrawalOperation> withdrawalOperationDao;

    private final AccountService accountService;

    private final UserService userService;

    private final CurrencyService currencyService;

    public WithdrawalOperationServiceImpl(OperationDao<WithdrawalOperation> withdrawalOperationDao,
                                          @Qualifier("AccountServiceImpl") AccountService accountService,
                                          @Qualifier("UserServiceImpl") UserService userService,
                                          CurrencyService currencyService) {
        this.withdrawalOperationDao = withdrawalOperationDao;
        this.accountService = accountService;
        this.userService = userService;
        this.currencyService = currencyService;
    }

    /**
     * Метод, обрабатывающий операцию снятия средств со счета {@link Account}
     * @param operation {@link WithdrawalOperation} - информация о снятия средств на счета
     */
    @Override
    @Transactional
    public void process(WithdrawalOperation operation) throws NotEnoughFundsInAccountException {
        Account account = accountService.getById(operation.getAccountId());
        BigDecimal withdrawalFunds = countWithdrawalFunds(operation.getCurrency(), account.getCurrency(), operation.getFunds());

        operation.setDateOfCreation(new Date());
        withdrawFundsFromAccount(account, withdrawalFunds);

        accountService.updateById(operation.getAccountId(), account);
        withdrawalOperationDao.save(operation);

        log.info("Saved withdrawal operation {}", operation);
    }

    /**
     * Метод возвращающий данные об операции снятия средств {@link WithdrawalOperation} со счета {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link WithdrawalOperation} - данные об операции
     */
    @Override
    public WithdrawalOperation getById(Integer id) {
        return withdrawalOperationDao.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt to get withdrawal operation by non-existent id"));
    }

    /**
     * Метод, возвращающий информацию о всех операциях списывания {@link WithdrawalOperation} со счета {@link Account} с идентификатором равным auditId
     * @param accountId - идентификатор счета, на информацию о списываниях которого идет запрос
     * @return {@link List<WithdrawalOperation>} - список операций пополнения
     *
     */
    @Override
    public List<WithdrawalOperation> getByAccountId(Integer accountId) {
        if (!accountService.isPresentById(accountId)) {
            throw new IllegalArgumentException("Attempt to get list of withdrawal operations by non-existent account id");
        } else return withdrawalOperationDao.getAllByAccountId(accountId);
    }

    /**
     * Метод, возвращающий информацию о всех операциях списывания {@link WithdrawalOperation}, совершенных пользователем {@link User}
     * с идентификатором равным userId
     * @param userId идентификатор пользователя, на информацию об операциях списываниях которого идет запрос
     * @return {@link List<WithdrawalOperation>} - список операций пополнения
     */
    @Override
    public List<WithdrawalOperation> getByUserId(Integer userId) {
        if (!userService.isPresentById(userId)) {
            throw  new IllegalArgumentException("Attempt to get list of withdrawal operations by non-existent user id");
        } else return withdrawalOperationDao.getAllByUserId(userId);
    }

    /**
     * Метод, выполняющий снятия средств со счета {@link Account}
     * @param account {@link Account} - счет, с которого нужно снять средства
     * @param funds - количество средств, которые нужно снять со счета
     * @throws NotEnoughFundsInAccountException - исключение, возникающее при попытке списать средства с пустого счета
     */
    private void withdrawFundsFromAccount(Account account, BigDecimal funds) throws NotEnoughFundsInAccountException {
        if (account.getFunds().compareTo(funds) < MINIMUM_AMOUNT_OF_FUNDS_IN_ACCOUNT) {
            throw new NotEnoughFundsInAccountException("Attempt to withdrawal off funds from an empty account");
        }
        BigDecimal newAmountOfFunds = account.getFunds().add(funds.negate());
        account.setFunds(newAmountOfFunds);
    }

    /**
     * Метод, рассчитывающий количество средств, которые нужно снять со счета {@link Account}
     * @param operationCurrency {@link Currency} - валюта средств, которые нужно снять со счета
     * @param accountCurrency {@link Currency} - валюта счета, с которого нужно снять средства
     * @param funds - количество средств в исходной волюте
     * @return количество средств, переведенные в валюту счета
     */
    private BigDecimal countWithdrawalFunds(Currency operationCurrency, Currency accountCurrency, BigDecimal funds) {
        BigDecimal exchangeRate = currencyService.getExchangeRate(operationCurrency, accountCurrency);
        return funds.multiply(exchangeRate);
    }

}
