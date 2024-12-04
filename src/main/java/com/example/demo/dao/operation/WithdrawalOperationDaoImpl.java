package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
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
 * Класс, реализующий функционал по работе с данными об операциях списывания {@link WithdrawalOperation}
 */
@Repository
@AllArgsConstructor
public class WithdrawalOperationDaoImpl implements OperationDao<WithdrawalOperation> {

    private final String SAVE_WITHDRAWAL_OPERATION =
            "INSERT INTO withdrawal_operations(id, user_id, account_id, date_of_creation, funds, currency) values(DEFAULT, ?, ?, ?, ?, ?)";

    private final String DELETE_WITHDRAWAL_OPERATION_BY_ID =
            "DELETE FROM withdrawal_operations WHERE id = ?";

    private final String GET_WITHDRAWAL_OPERATION_BY_ID =
            "SELECT * FROM withdrawal_operations WHERE id = ?";

    private final String GET_WITHDRAWAL_OPERATIONS_BY_USER_ID =
            "SELECT * FROM withdrawal_operations WHERE user_id = ?";

    private final String GET_WITHDRAWAL_OPERATIONS_BY_ACCOUNT_ID =
            "SELECT * FROM withdrawal_operations WHERE account_id = ?";

    private final DataSource dataSource;

    /**
     * Метод, сохраняющий данные об операциях списывания {@link WithdrawalOperation}
     * @param operation {@link WithdrawalOperation} - операция списывания
     * @throws SQLException - исключение, возникающее при сохранении данных об операциях списывания
     */
    @Override
    public void save(WithdrawalOperation operation) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(SAVE_WITHDRAWAL_OPERATION)) {
            Date date = new Date(operation.getDateOfCreation().getTime());

            prepareStatement.setInt(1, operation.getUserId());
            prepareStatement.setInt(2, operation.getAccountId());
            prepareStatement.setDate(3, date);
            prepareStatement.setBigDecimal(4, operation.getFunds());
            prepareStatement.setShort(5, operation.getCurrency().getNumber());

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, удаляющий данные об операции списывания {@link WithdrawalOperation} с идентификатором равным id
     * @param id - идентификатор операции списывания
     * @throws SQLException - исключение, возникшее при удалении данных об операциях списывания
     */
    @Override
    public void deleteById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(DELETE_WITHDRAWAL_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, возвращающий данные об операции списывания {@link WithdrawalOperation} с идентификатором равным id
     * @param id - идентификатор операции списывания
     * @return {@link Optional<WithdrawalOperation>} - информация об операции списывания
     * @throws SQLException - исключение, возникшее при получении данных об операции списывания
     */
    @Override
    public Optional<WithdrawalOperation> getById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_WITHDRAWAL_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<WithdrawalOperation> operation = resultSet.next() ?
                    Optional.of(getWithdrawalOperationFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return operation;
        }
    }

    /**
     * Метод, возвращающий данные об операциях списывания {@link WithdrawalOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<WithdrawalOperation>} - список операций
     * @throws SQLException - исключение, возникшее при получении данных об операциях списывания
     */
    @Override
    public List<WithdrawalOperation> getAllByAccountId(Integer accountId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_WITHDRAWAL_OPERATIONS_BY_ACCOUNT_ID)) {

            prepareStatement.setInt(1, accountId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<WithdrawalOperation> withdrawalOperations = new ArrayList<>();

            while (resultSet.next()) {
                withdrawalOperations.add(getWithdrawalOperationFromResultSet(resultSet));
            }

            resultSet.close();
            return withdrawalOperations;
        }
    }

    /**
     * Метод, возвращающий данные об операциях {@link WithdrawalOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<WithdrawalOperation>} - список операций списывания
     * @throws SQLException - исключение, возникшее при получении данных об операциях списывания
     */
    @Override
    public List<WithdrawalOperation> getAllByUserId(Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_WITHDRAWAL_OPERATIONS_BY_USER_ID)) {

            prepareStatement.setInt(1, userId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<WithdrawalOperation> withdrawalOperations = new ArrayList<>();

            while (resultSet.next()) {
                withdrawalOperations.add(getWithdrawalOperationFromResultSet(resultSet));
            }

            resultSet.close();
            return withdrawalOperations;
        }
    }

    /**
     * Метод, получающий данные об операции списывания {@link WithdrawalOperation} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о счетах из БД
     * @return {@link WithdrawalOperation} информация об операции списывания
     * @throws SQLException - исключение, возникшее при чтении данных из БД об операции списывания
     */
    private WithdrawalOperation getWithdrawalOperationFromResultSet(ResultSet resultSet) throws SQLException {
        return WithdrawalOperation.builder()
                .id(resultSet.getInt("id"))
                .userId(resultSet.getInt("user_id"))
                .accountId(resultSet.getInt("account_id"))
                .dateOfCreation(resultSet.getDate("date_of_creation"))
                .funds(resultSet.getBigDecimal("funds"))
                .currency(Currency.getByNumber(resultSet.getShort("currency")))
                .build();
    }

}
