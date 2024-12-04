package com.example.demo.dao.account;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс, реализующий функционал по работе с данными о счетах {@link Account}
 */
@Repository
@AllArgsConstructor
public class AccountDaoImpl implements AccountDao {

    private final String SAVE_ACCOUNT =
            "INSERT INTO accounts(id, user_id, date_of_creation, funds, currency) values(DEFAULT, ?, ?, ?, ?)";

    private final String DELETE_ACCOUNT_BY_ID =
            "DELETE FROM accounts WHERE id = ?";

    private final String UPDATE_ACCOUNT_BY_ID =
            "UPDATE accounts SET date_of_creation = ?, funds = ?, currency = ? WHERE id = ?";

    private final String GET_ACCOUNT_BY_ID =
            "SELECT * FROM accounts WHERE id = ?";

    private final String GET_ACCOUNTS_BY_USER_ID =
            "SELECT * FROM accounts WHERE user_id = ?";

    private final DataSource dataSource;

    /**
     * Метод, сохраняющий данные о счете {@link Account}
     * @param account - данные о счете
     * @throws SQLException - исключение, возникающее при сохранении данных о счете
     */
    @Override
    public void save(Account account) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(SAVE_ACCOUNT)) {
             Date date = new Date(account.getDateOfCreation().getTime());

             prepareStatement.setInt(1, account.getUserId());
             prepareStatement.setDate(2, date);
             prepareStatement.setBigDecimal(3, account.getFunds());
             prepareStatement.setShort(4, account.getCurrency().getNumber());

             prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, удаляющий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     * @throws SQLException - исключение, возникшее при удалении данных о счете
     */
    @Override
    public void deleteById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(DELETE_ACCOUNT_BY_ID)) {

            prepareStatement.setInt(1, id);

            prepareStatement.executeUpdate();
        }
    }


    /**
     * Метод, обновляющий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     * @param account {@link Account} - новые данные о счете
     * @throws SQLException - исключение, возникшее при обновлении данных о счете
     */
    @Override
    public void updateById(Integer id, Account account) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(UPDATE_ACCOUNT_BY_ID)) {
            Date date = new Date(account.getDateOfCreation().getTime());

            prepareStatement.setDate(1, date);
            prepareStatement.setBigDecimal(2, account.getFunds());
            prepareStatement.setShort(3, account.getCurrency().getNumber());
            prepareStatement.setInt(4, id);

            prepareStatement.executeUpdate();
        }
    }

    /**
     /**
     * Метод, возвращающий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     * @throws SQLException - исключение, возникшее при получении данных о счете
     */
    @Override
    public Optional<Account> getById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(GET_ACCOUNT_BY_ID)) {

            prepareStatement.setInt(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<Account> account = resultSet.next() ?
                    Optional.of(getAccountFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return account;
        }
    }

    /**
     * Метод, возвращающий данные о счетах {@link Account}, принадлежащих пользователю {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<Account>} - список счетов
     * @throws SQLException - исключение, возникшее при получении данных о счетах пользователя
     */
    @Override
    public List<Account> getByUserId(Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(GET_ACCOUNTS_BY_USER_ID)) {

            prepareStatement.setInt(1, userId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<Account> accounts = new ArrayList<>();

            while (resultSet.next()) {
                accounts.add(getAccountFromResultSet(resultSet));
            }

            return accounts;
        }
    }

    /**
     * Метод, получающий данные о счете {@link Account} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о счетах из БД
     * @return {@link Account} информация о счете
     * @throws SQLException - исключение, возникшее при получении данных о счете
     */
    private Account getAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return Account.builder()
                .id(resultSet.getInt("id"))
                .userId(resultSet.getInt("user_id"))
                .dateOfCreation(resultSet.getDate("date_of_creation"))
                .funds(resultSet.getBigDecimal("funds"))
                .currency(Currency.getByNumber(resultSet.getShort("currency")))
                .build();
    }

}
