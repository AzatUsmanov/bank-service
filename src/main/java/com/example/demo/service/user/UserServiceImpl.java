package com.example.demo.service.user;

import com.example.demo.dao.user.UserDao;
import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.authority.AuthorityService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Класс, выполняющий бизнес логику по работе с пользователями {@link User}
 */
@Slf4j
@Service("UserServiceImpl")
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao dao;

    private final AuthorityService authorityService;


    /**
     * Метод, сохраняющий пользователя {@link User}
     * @param user - {@link User} пользователь для сохранения
     * @throws NotUniqueEmailException - исключение, возникающее при попытке сохранить пользователя с неуникальной почтой
     * @throws NotUniqueUsernameException исключение, возникающее при попытке сохранить пользователя с неуникальным именем
     */
    @Override
    public void create(User user) throws NotUniqueEmailException, NotUniqueUsernameException {
        try {
            checkUserForUniqueness(user);
            dao.save(user);
            saveAuthoritiesFromUser(user);
            log.info("User saved {}", user);
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while saving user data", e);
        }
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных о пользователе
     * и вызывающий метод возвращающий пользователя {@link User}.
     * @param id - идентификатор пользователя, по которому идет запрос на чтение
     * @return {@link User} - данные о пользователе, с идентификатором равным id
     */
    @Override
    public User getById(Integer id) {
        try {
            return dao.getById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Search for user with non-existent id"));
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting user by ID", e);
        }
    }

    /**
     * Метод, возвращает данные о пользователе {@link User}
     * @param username - имя пользователя, по которому идет запрос на чтение
     * @return {@link Optional<User>} - данные о пользователе, с именем равным username
     */
    @Override
    public Optional<User> findByUsername(String username) {
        try {
            return dao.getByUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting user by ID", e);
        }
    }

    /**
     * Метод, проверяющий существование пользователя {@link User}
     * @param id - идентификатор счета, наличие которого проверяется
     * @return true, если пользователь существует и false, если нет
     */
    @Override
    public boolean isPresentById(Integer id) {
        try {
            return dao.getById(id).isPresent();
        } catch (SQLException e) {
            throw new RuntimeException("An exception occurred while getting user by ID", e);
        }
    }

    /**
     * Метод, проверяющий уникальность пользователя
     * @param user {@link User} - пользователь для сохранения
     * @throws NotUniqueEmailException - исключение, возникающее, если у пользователя неуникальная почта
     * @throws SQLException - исключение, возникающее при работе с данными
     * @throws NotUniqueUsernameException исключение, возникающее, если у пользователя неуникальное имя
     */
    private void checkUserForUniqueness(User user) throws NotUniqueEmailException, SQLException, NotUniqueUsernameException {
         if (dao.getByMail(user.getEmail()).isPresent()) {
             throw new NotUniqueEmailException("Received non-unique email for registration");
         } else if (dao.getByUsername(user.getUsername()).isPresent()) {
             throw new NotUniqueUsernameException("Received non-unique username for registration");
         }
    }

    /**
     * Метод сохраняющий данные о полномочиях пользователя
     * @param user {@link User} - данные о пользователе
     */
    private void saveAuthoritiesFromUser(User user) {
        Integer userId = findByUsername(user.getUsername())
                .orElseThrow(IllegalArgumentException::new)
                .getId();
        List<Authority> authorities = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(Authority::valueOf)
                .toList();

        authorityService.saveByUserId(authorities, userId);
    }

}
