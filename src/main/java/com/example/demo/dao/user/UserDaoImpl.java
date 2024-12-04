package com.example.demo.dao.user;

import com.example.demo.domain.model.User;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final String GET_USER_BY_MAIL =
            "SELECT * FROM users WHERE email = ?";

    private final String GET_USER_BY_USERNAME =
            "SELECT * FROM users WHERE username = ?";

    private final DataSource dataSource;

    /**
     * Метод, сохраняющий данные о пользователе {@link User}
     * @param user - данные о пользователе
     * @throws SQLException - исключение, возникающее при сохранении данных о пользователе
     */
    @Override
    public void save(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_USER)) {

            prepareStatement.setString(1, user.getUsername());
            prepareStatement.setString(2, user.getEmail());
            prepareStatement.setString(3, user.getPassword());

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, возвращающий данные о пользователе {@link User} с идентификатором равным id
     * @param id - идентификатор пользователя
     * @return {Optional<User>} - информация о пользователе
     * @throws SQLException - исключение, возникшее при получении данных о пользователе
     */
    @Override
    public Optional<User> getById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(GET_USER_BY_ID)) {

            prepareStatement.setInt(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<User> user = resultSet.next() ?
                    Optional.of(getUserFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return user;
        }
    }

    /**
     * Метод, возвращающий данные о пользователе {@link User} с именем равным username
     * @param username - имя пользователя
     * @return {Optional<User>} - информация о пользователе
     * @throws SQLException - исключение, возникшее при получении данных о пользователе
     */
    @Override
    public Optional<User> getByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(GET_USER_BY_USERNAME)) {

            prepareStatement.setString(1, username);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<User> user = resultSet.next() ?
                    Optional.of(getUserFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return user;
        }
    }

    /**
     * Метод, возвращающий данные о пользователе {@link User} с почтой равной mail
     * @param mail - почта пользователя
     * @return {Optional<User>} - информация о пользователе
     * @throws SQLException - исключение, возникшее при получении данных о пользователе
     */
    @Override
    public Optional<User> getByMail(String mail) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(GET_USER_BY_MAIL)) {

            prepareStatement.setString(1, mail);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<User> user = resultSet.next() ?
                    Optional.of(getUserFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return user;
        }
    }

    /**
     * Метод, получающий данные о пользователе {@link User} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о пользователях из БД
     * @return {@link User} - данные о пользователе
     * @throws SQLException - исключение, возникшее при чтении данных из БД о пользователе
     */
    private User getUserFromResultSet(ResultSet resultSet) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .username(resultSet.getString("username"))
                .email(resultSet.getString("email"))
                .password(resultSet.getString("password"))
                .build();
    }
}
