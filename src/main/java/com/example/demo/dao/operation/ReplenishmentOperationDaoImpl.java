package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
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
 * Класс, реализующий функционал по работе с данными об операциях пополнения {@link ReplenishmentOperation}
 */
@Repository
@AllArgsConstructor
public class ReplenishmentOperationDaoImpl implements OperationDao<ReplenishmentOperation> {

    private final String SAVE_REPLENISHMENT_OPERATION =
            "INSERT INTO replenishment_operations(id, user_id, account_id, date_of_creation, funds, currency) values(DEFAULT, ?, ?, ?, ?, ?)";

    private final String DELETE_REPLENISHMENT_OPERATION_BY_ID =
            "DELETE FROM replenishment_operations WHERE id = ?";

    private final String GET_REPLENISHMENT_OPERATION_BY_ID =
            "SELECT * FROM replenishment_operations WHERE id = ?";

    private final String GET_REPLENISHMENT_OPERATIONS_BY_USER_ID =
            "SELECT * FROM replenishment_operations WHERE user_id = ?";

    private final String GET_REPLENISHMENT_OPERATIONS_BY_ACCOUNT_ID =
            "SELECT * FROM replenishment_operations WHERE account_id = ?";

    private final DataSource dataSource;

    /**
     * Метод, сохраняющий данные об операциях пополнения {@link ReplenishmentOperation}
     * @param operation {@link ReplenishmentOperation} - операция пополнения
     * @throws SQLException - исключение, возникающее при сохранении данных об операциях пополнения
     */
    @Override
    public void save(ReplenishmentOperation operation) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(SAVE_REPLENISHMENT_OPERATION)) {
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
     * Метод, удаляющий данные об операции пополнения {@link ReplenishmentOperation} с идентификатором равным id
     * @param id - идентификатор операции пополнения
     * @throws SQLException - исключение, возникшее при удалении данных об операциях пополнения
     */
    @Override
    public void deleteById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(DELETE_REPLENISHMENT_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, возвращающий данные об операции пополнения {@link ReplenishmentOperation} с идентификатором равным id
     * @param id - идентификатор операции пополнения
     * @return {@link Optional<ReplenishmentOperation>} - информация об операции пополнении
     * @throws SQLException - исключение, возникшее при получении данных об операции пополнения
     */
    @Override
    public Optional<ReplenishmentOperation> getById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_REPLENISHMENT_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<ReplenishmentOperation> operation = resultSet.next() ?
                    Optional.of(getReplenishmentOperationFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return operation;
        }
    }

    /**
     * Метод, возвращающий данные об операциях пополнения {@link ReplenishmentOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<ReplenishmentOperation>} - список операций
     * @throws SQLException - исключение, возникшее при получении данных об операциях пополнения
     */
    @Override
    public List<ReplenishmentOperation> getAllByAccountId(Integer accountId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_REPLENISHMENT_OPERATIONS_BY_ACCOUNT_ID)) {

            prepareStatement.setInt(1, accountId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<ReplenishmentOperation> replenishmentOperations = new ArrayList<>();

            while (resultSet.next()) {
                replenishmentOperations.add(getReplenishmentOperationFromResultSet(resultSet));
            }

            resultSet.close();
            return replenishmentOperations;
        }
    }

    /**
     * Метод, возвращающий данные об операциях {@link ReplenishmentOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<ReplenishmentOperation>} - список операций
     * @throws SQLException - исключение, возникшее при получении данных об операциях пополнения
     */
    @Override
    public List<ReplenishmentOperation> getAllByUserId(Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_REPLENISHMENT_OPERATIONS_BY_USER_ID)) {

            prepareStatement.setInt(1, userId);

            ResultSet resultSet = prepareStatement.executeQuery();
            List<ReplenishmentOperation> replenishmentOperations = new ArrayList<>();

            while (resultSet.next()) {
                replenishmentOperations.add(getReplenishmentOperationFromResultSet(resultSet));
            }

            resultSet.close();
            return replenishmentOperations;

        }
    }

    /**
     * Метод, получающий данные об операции пополнения {@link ReplenishmentOperation} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о счетах из БД
     * @return {@link ReplenishmentOperation} информация об операции пополнения
     * @throws SQLException - исключение, возникшее при чтении данных из БД об операции пополнения
     */
    private ReplenishmentOperation getReplenishmentOperationFromResultSet(ResultSet resultSet) throws SQLException {
        return ReplenishmentOperation.builder()
                .id(resultSet.getInt("id"))
                .userId(resultSet.getInt("user_id"))
                .accountId(resultSet.getInt("account_id"))
                .dateOfCreation(resultSet.getDate("date_of_creation"))
                .funds(resultSet.getBigDecimal("funds"))
                .currency(Currency.getByNumber(resultSet.getShort("currency")))
                .build();
    }

}
