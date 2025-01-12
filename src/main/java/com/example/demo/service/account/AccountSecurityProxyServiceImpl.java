package com.example.demo.service.account;

import com.example.demo.domain.dto.Account;
import com.example.demo.service.authentication.CurrentUserService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Класс, реализующий логику проверки доступа к данным о счетах {@link Account}
 */
@Service("AccountSecurityProxyServiceImpl")
public class AccountSecurityProxyServiceImpl implements  AccountService {

    private final AccountService accountService;

    private final CurrentUserService currentUserService;

    public AccountSecurityProxyServiceImpl(@Qualifier("AccountServiceImpl") AccountService accountService,
                                           CurrentUserService currentUserService) {
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, проверяющий доступ пользователя к созданию счета
     * и вызывающий метод сохраняющий счет {@link Account}.
     * @param account - {@link Account} счет для сохранения.
     */
    @Override
    public void save(Account account) {
        if (currentUserService.userHasNoAuthorityToEdit(account.getUserId())) {
            throw new AccessDeniedException("Attempt to save an account for another user");
        }
        accountService.save(account);
    }

    /**
     * Метод, проверяющий доступ пользователя к редактированию счета
     * и вызывающий метод обновляющий счет {@link Account}.
     * @param id - идентификатор счета, который нужно обновить
     * @param account - {@link Account} счет для обновления
     */
    @Override
    public void updateById(Integer id, Account account) {
        if (!accountService.isPresentById(id)) {
            throw new AccessDeniedException("Attempt to update by non-existent account");
        }
        if (currentUserService.userHasNoAuthorityToEdit(account.getUserId())) {
            throw new AccessDeniedException("Attempt to update an account for another user");
        }
        accountService.updateById(id, account);
    }

    /**
     * Метод, проверяющий доступ пользователя к удалению счета
     * и вызывающий метод удаляющий счет {@link Account}.
     * @param id - идентификатор счета, который нужно удалить
     */
    @Override
    public void deleteById(Integer id) {
        if (!accountService.isPresentById(id)) {
            throw new AccessDeniedException("Attempt to delete an non-existent account");
        }
        Account account = accountService.getById(id);
        if (currentUserService.userHasNoAuthorityToEdit(account.getUserId())) {
            throw new AccessDeniedException("Attempt to delete an account for another user");
        }
        accountService.deleteById(id);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о счете
     * и вызывающий метод, возвращающий счет {@link Account}.
     * @param id - идентификатор счета, по которому идет запрос на чтение
     * @return {@link Account} - счет, который возвращает метод
     */
    @Override
    public Account getById(Integer id) {
        if (!accountService.isPresentById(id)) {
            throw new AccessDeniedException("Attempt to get an non-existent account");
        }
        Account account = accountService.getById(id);
        if (currentUserService.userHasNoAuthorityToView(account.getUserId())) {
            throw new AccessDeniedException("Attempt to get an account for another user");
        }
        return account;
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о счетах пользователя
     * и вызывающий метод, возвращающий данные о счетах.
     * @param userId - идентификатор пользователя, по которому идет запрос на чтение
     * @return {@link List<Account>} - список счетов, принадлежащий пользователю с идентификатором равным userId
     */
    @Override
    public List<Account> getByUserId(Integer userId) {
        if (currentUserService.userHasNoAuthorityToView(userId)) {
            throw new AccessDeniedException("Attempt to get an account for another user");
        }
        return accountService.getByUserId(userId);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о счете
     * и вызывающий метод возвращающий данные о существовании счета.
     * @param id - идентификатор счета, наличие которого проверяется
     * @return true, если счет существует и false, если нет
     */
    @Override
    public boolean isPresentById(Integer id) {
        Account account = accountService.getById(id);
        if (currentUserService.userHasNoAuthorityToView(account.getUserId())) {
            throw new AccessDeniedException("Attempt to get an account for another user");
        }
        return accountService.isPresentById(id);
    }

}
