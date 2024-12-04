package com.example.demo.dao.authority;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Authority;
import lombok.AllArgsConstructor;
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

    private final DataSource dataSource;

    /**
     * Метод, возвращающий данные о полномочиях {@link Authority} пользователя {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<Authority>} - список полномочий пользователя
     * @throws SQLException - исключение, возникшее при получении данных о полномочиях
     */
    @Override
    public List<Authority> getByUserId(Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_AUTHORITIES_BY_USER_ID)) {

            prepareStatement.setInt(1, userId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<Authority> authorities = new ArrayList<>();

            while (resultSet.next()) {
                authorities.add(getAuthorityFromResultSet(resultSet));
            }

            resultSet.close();
            return authorities;
        }
    }


    /**
     * Метод, сохраняющий данные о полномочие {@link Authority} пользователя {@link User}
     * @param authority - полномочие пользователя
     * @param userId - идентификатор пользователя
     * @throws SQLException - исключение, возникшее при получении данных о полномочиях
     */
    @Override
    public void saveByUserId(Authority authority, Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(SAVE_AUTHORITY)) {

            prepareStatement.setInt(1, userId);
            prepareStatement.setShort(2, authority.getNumber());

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, получающий данные о полномочии {@link Authority} пользователя {@link User} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о полномочиях из БД
     * @return {@link Authority} - информация о полномочии
     * @throws SQLException - исключение, возникшее при чтении данных из БД о Полномочии
     */
    private Authority getAuthorityFromResultSet(ResultSet resultSet) throws SQLException {
        short number = resultSet.getShort("Authority");
        return Arrays.stream(Authority.values())
                .filter(x -> x.getNumber() == number)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}