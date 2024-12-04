package com.example.demo.service.user;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.User;
import com.example.demo.service.authentication.CurrentUserService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("UserSecurityProxyServiceImpl")
public class UserSecurityProxyServiceImpl implements UserService {

    private final UserService userService;

    private final CurrentUserService currentUserService;

    public UserSecurityProxyServiceImpl(@Qualifier("UserServiceImpl") UserService userService,
                                        CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, вызывающий метод сохраняющий пользователя
     * @param user - {@link User} пользователь для сохранения
     * @throws NotUniqueEmailException - исключение, возникающее при попытке сохранить пользователя с неуникальной почтой
     * @throws NotUniqueUsernameException исключение, возникающее при попытке сохранить пользователя с неуникальным именем
     */
    @Override
    public void create(User user) throws NotUniqueEmailException, NotUniqueUsernameException {
        userService.create(user);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о пользователе
     * и вызывающий метод, возвращающий пользователя {@link User}.
     * @param id - идентификатор пользователя, по которому идет запрос на чтение
     * @return {@link User} - данные о пользователе, с идентификатором равным id
     */
    @Override
    public User getById(Integer id) {
        if (!currentUserService.userHasAuthorityToView(id)) {
            throw new AccessDeniedException("Attempt to get an account for another user by id");
        }
        return userService.getById(id);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о пользователе
     * и вызывающий метод, возвращающий пользователя {@link User}.
     * @param username - имя пользователя, по которому идет запрос на чтение
     * @return {@link Optional<User>} - данные о пользователе, с именем равным username
     */
    @Override
    public Optional<User> findByUsername(String username) {
        if (currentUserService.userHasAuthorityToView(username)) {
            throw new AccessDeniedException("Attempt to get an account for another user by username");
        }
        return userService.findByUsername(username);
    }


    /**
     * Метод, проверяющий доступ пользователя к чтению данных о пользователе
     * и вызывающий метод, проверяющий существование пользователя {@link User}
     * @param id - идентификатор счета, наличие которого проверяется
     * @return true, если пользователь существует и false, если нет
     */
    @Override
    public boolean isPresentById(Integer id) {
        if (!currentUserService.userHasAuthorityToView(id)) {
            throw new AccessDeniedException("Trying to check the existence of another user");
        }
        return userService.isPresentById(id);
    }
}
