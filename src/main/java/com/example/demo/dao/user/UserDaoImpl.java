package com.example.demo.dao.user;

import com.example.demo.domain.model.User;

import lombok.AllArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Класс, реализующий функционал по работе с данными о пользователях {@link User}
 */
@Repository
@AllArgsConstructor
public class UserDaoImpl implements UserDao {

    private final String SAVE_USER =
            "INSERT INTO users(id, username, email, password) values(DEFAULT, ?, ?, ?)";

    private final String GET_USER_BY_ID =
            "SELECT * FROM users WHERE id = ?";

    private final String GET_USER_BY_USERNAME =
            "SELECT * FROM users WHERE username = ?";

    private final String GET_USER_BY_EMAIL =
            "SELECT * FROM users WHERE email = ?";


    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        return User.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .build();
    };

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод, сохраняющий данные о пользователе {@link User}
     * @param user {@link User} - данные о пользователе
     */
    @Override
    public void save(User user) {
        jdbcTemplate.update(SAVE_USER,
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
    }

    /**
     * Метод, возвращающий данные о пользователе {@link User} с идентификатором равным id
     * @param id - идентификатор пользователя
     * @return User {@link User} - информация о пользователе
     */
    @Override
    public Optional<User> getById(Integer id) {
        List<User> users = jdbcTemplate.query(GET_USER_BY_ID, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

    /**
     * Метод, возвращающий данные о пользователе {@link User} с именем равным username
     * @param username - имя пользователя
     * @return User {@link User} - информация о пользователе
     */
    @Override
    public Optional<User> getByUsername(String username)  {
        List<User> users = jdbcTemplate.query(GET_USER_BY_USERNAME, userRowMapper, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }


    /**
     * Метод, возвращающий данные о пользователе {@link User} с почтой равной mail
     * @param email - почта пользователя
     * @return {@link Optional<User>} - информация о пользователе
     */
    @Override
    public Optional<User> getByEmail(String email)  {
        List<User> users = jdbcTemplate.query(GET_USER_BY_EMAIL, userRowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

}
