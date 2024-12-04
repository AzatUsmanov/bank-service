package com.example.demo.service.authentication;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Класс выполняющий логику по предоставлению информации о текущем пользователе
 */
@Service
public class CurrentUserServiceImpl implements  CurrentUserService {

    /**
     * Метод, проверяющий имеет ли текущий пользователь право на редактирование информации, связанной
     * с пользователем с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return true, если текущему пользователю можно редактировать и false, если нет
     */
    @Override
    public boolean userHasNoAuthorityToEdit(Integer userId) {
        return notEqualToCurrentUserId(userId) && !currentUserHasAuthority(Authority.ADMIN_EDIT);
    }

    /**
     * Метод, проверяющий имеет ли текущий пользователь право на чтение информации, связанной
     * с пользователем с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return true, если текущему пользователю можно читать и false, если нет
     */
    @Override
    public boolean userHasNoAuthorityToView(Integer userId) {
        return notEqualToCurrentUserId(userId) && !currentUserHasAuthority(Authority.ADMIN_VIEW);
    }

    /**
     * Метод, проверяющий имеет ли текущий пользователь право на чтение информации, связанной
     * с пользователем с именем равным username
     * @param username - имя пользователя
     * @return true, если текущему пользователю можно читать и false, если нет
     */
    @Override
    public boolean userHasAuthorityToView(String username) {
        return equalToCurrentUsername(username) || currentUserHasAuthority(Authority.ADMIN_VIEW);
    }

    /**
     * Метод, проверяющий равен ли id идентификатору текущего пользователя
     * @param id - идентификатор для сравнения
     * @return true, ли равны и false, если нет
     */
    @Override
    public boolean notEqualToCurrentUserId(Integer id) {
        return !Objects.equals(getCurrentUserId(), id);
    }

    /**
     * Метод, проверяющий равен ли username имени текущего пользователя
     * @param username - имя пользователя для сравнения
     * @return true, ли равны и false, если нет
     */
    @Override
    public boolean equalToCurrentUsername(String username) {
        return Objects.equals(getCurrentUsername(), username);
    }

    /**
     * Метод, если у пользователя есть полномочие authority
     * @param authority - полномочие для проверки
     * @return true, если у пользователя есть это полномочие и false, если нет
     */
    @Override
    public boolean currentUserHasAuthority(Authority authority) {
        return getCurrentUser()
                .getUserAuthorities()
                .contains(authority);
    }

    /**
     * Метод, возвращающий идентификатор текущего пользователя
     * @return идентификатор
     */
    @Override
    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Метод, возвращающий имя текущего пользователя
     * @return имя пользователя
     */
    @Override
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Метод, возвращающий текущего пользователя {@link User}
     * @return {@link User} - информация о пользователе
     */
    @Override
    public User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
