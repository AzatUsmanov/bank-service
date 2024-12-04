package com.example.demo.service.authority;

import com.example.demo.dao.authority.AuthorityDao;
import com.example.demo.domain.model.Authority;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс, выполняющий бизнес-логику по работе с полномочиями пользователя
 */
@Service
@AllArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityDao authorityDao;

    /**
     * Метод, сохраняющий все полномочия пользователя
     * @param authorities {@link Authority} - полномочия для сохранения
     * @param userId - идентификатор пользователя, полномочия которого нужно сохранить
     */
    @Override
    public void saveByUserId(List<Authority> authorities, Integer userId) {
        authorities.forEach(x -> {
            try {
                authorityDao.saveByUserId(x, userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Метод, возвращающий список полномочий пользователя
     * @param userId - идентификатор пользователя, полномочия которого нужно вернуть
     * @return {@link List<Authority>} - список полномочий пользователя
     */
    @Override
    public List<Authority> getByUserId(Integer userId) {
        try {
            return authorityDao.getByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
