package com.example.demo.dao.authority;

import com.example.demo.domain.model.Authority;

import lombok.AllArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Класс, реализующий функционал по работе с данными о полномочия пользователей {@link Authority}
 */
@Repository
@AllArgsConstructor
public class AuthorityDaoImpl implements AuthorityDao {

    private final String GET_AUTHORITIES_BY_USER_ID =
            "SELECT * FROM authorities WHERE user_id = ?";

    private final String SAVE_AUTHORITY =
            "INSERT INTO authorities(id, user_id, authority) values(DEFAULT, ?, ?)";

    private final RowMapper<Authority> authorityRowMapper = (rs, rowNum) -> {
        Short authority = rs.getShort("authority");
        return Arrays.stream(Authority.values())
                .filter(x -> x.getNumber() == authority)
                .findFirst()
                .get();
    };

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод, возвращающий данные о полномочиях {@link Authority} пользователя {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<Authority>} - список полномочий пользователя
     */
    @Override
    public List<Authority> getByUserId(Integer userId) {
        return jdbcTemplate.query(GET_AUTHORITIES_BY_USER_ID, authorityRowMapper, userId);
    }


    /**
     * Метод, сохраняющий данные о полномочие {@link Authority} пользователя {@link User}
     * @param authority - полномочие пользователя
     * @param userId - идентификатор пользователя
     */
    @Override
    public void saveByUserId(Authority authority, Integer userId) {
        jdbcTemplate.update(SAVE_AUTHORITY, userId, authority.getNumber());
    }

}
