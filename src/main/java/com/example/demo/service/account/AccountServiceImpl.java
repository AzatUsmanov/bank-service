package com.example.demo.service.account;

import com.example.demo.dao.account.AccountDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.service.authentication.CurrentUserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Класс, выполняющий бизнес логику по работе со счетами {@link Account}
 */
@Slf4j
@Service("AccountServiceImpl")
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;


    private final CurrentUserService currentUserService;

    public AccountServiceImpl(AccountDao accountDao, CurrentUserService currentUserService) {
        this.accountDao = accountDao;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, сохраняющий счет {@link Account}
     * @param account - {@link Account} счет для сохранения
     */
    @Override
    public void save(Account account) {
        account.setUserId(currentUserService.getCurrentUserId());
        account.setFunds(new BigDecimal("0.0"));
        account.setDateOfCreation(new Date());
        accountDao.save(account);
        log.info("Account saved {}", account);
    }

    /**
     * Метод, обновляющий данные об счете {@link Account}
     * @param id - идентификатор счета для обновления счета
     * @param account - {@link Account} счет для обновления
     */
    @Override
    public void updateById(Integer id, Account account) {
        if (!isPresentById(id)) {
            throw new IllegalArgumentException("Accessing an account with a non-existent id");
        }
        accountDao.updateById(id, account);
        log.info("Account with id = {} updated to {}", id, account);
    }

    /**
     * Метод удаляющий данные о счете {@link Account}
     * @param id - идентификатор счета, который нужно удалить
     */
    @Override
    public void deleteById(Integer id) {
        if (!isPresentById(id)) {
            throw new IllegalArgumentException("Accessing an account with a non-existent id");
        }
        accountDao.deleteById(id);
        log.info("Account with id = {} deleted", id);
    }

    /**
     * Метод, возвращает данные о счете {@link Account}
     * @param id - идентификатор счета, по которому идет запрос на чтение
     * @return {@link Account} - данные о счете с идентификатором равным id
     */
    @Override
    public Account getById(Integer id) {
        return accountDao.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Search for account with non-existent id"));
    }

    /**
     * Метод, возвращает данные о счетах пользователя {@link Account}
     * @param userId - идентификатор пользователя, по которому идет запрос на чтение
     * @return {@link List<Account>} - список счетов, принадлежащий пользователю с идентификатором равным userId
     */
    @Override
    public List<Account> getByUserId(Integer userId) {
        return accountDao.getByUserId(userId);
    }

    /**
     * Метод, проверяющий существование счета {@link Account}
     * @param id - идентификатор счета, наличие которого проверяется
     * @return true, если счет существует и false, если нет
     */
    @Override
    public boolean isPresentById(Integer id) {
        return accountDao.getById(id).isPresent();
    }

}
