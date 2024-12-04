package com.example.demo.service.account;

import com.example.demo.dao.account.AccountDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс, выполняющий бизнес логику по работе со счетами {@link Account}
 */
@Slf4j
@Service("AccountServiceImpl")
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;

    private final UserService userService;

    public AccountServiceImpl(AccountDao accountDao,
                              @Qualifier("UserServiceImpl") UserService userService) {
        this.accountDao = accountDao;
        this.userService = userService;
    }

    /**
     * Метод, сохраняющий счет {@link Account}
     * @param account - {@link Account} счет для сохранения
     */
    @Override
    public void create(Account account) {
        try {
            accountDao.save(account);
            log.info("Account saved {}", account);
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while creating an account", e);
        }
    }

    /**
     * Метод, обновляющий данные об счете {@link Account}
     * @param id - идентификатор счета для обновления счета
     * @param account - {@link Account} счет для обновления
     */
    @Override
    public void updateById(Integer id, Account account) {
        try {
            if (!isPresentById(id)) {
                throw new IllegalArgumentException("Accessing an account with a non-existent id");
            }
            accountDao.updateById(id, account);
            log.info("Account with id = {} updated to {}", id, account);
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while updating an account", e);
        }
    }

    /**
     * Метод удаляющий данные о счете {@link Account}
     * @param id - идентификатор счета, который нужно удалить
     */
    @Override
    public void deleteById(Integer id) {
        try {
            if (!isPresentById(id)) {
                throw new IllegalArgumentException("Accessing an account with a non-existent id");
            }
            accountDao.deleteById(id);
            log.info("Account with id = {} deleted", id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод, возвращает данные о счете {@link Account}
     * @param id - идентификатор счета, по которому идет запрос на чтение
     * @return {@link Account} - данные о счете с идентификатором равным id
     */
    @Override
    public Account getById(Integer id) {
        try {
            return accountDao.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Search for account with non-existent id"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод, возвращает данные о счетах пользователя {@link Account}
     * @param userId - идентификатор пользователя, по которому идет запрос на чтение
     * @return {@link List<Account>} - список счетов, принадлежащий пользователю с идентификатором равным userId
     */
    @Override
    public List<Account> getByUserId(Integer userId) {
        try {
            if (userService.isPresentById(userId)) {
                return accountDao.getByUserId(userId);
            } else {
                throw new IllegalArgumentException("An exception occurred while searching for accounts for a non-existent user id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод, проверяющий существование счета {@link Account}
     * @param id - идентификатор счета, наличие которого проверяется
     * @return true, если счет существует и false, если нет
     */
    @Override
    public boolean isPresentById(Integer id) {
        try {
            return accountDao.getById(id).isPresent();
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting account by id", e);
        }
    }

}
