package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.TransferOperation;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Класс, реализующий функционал по работе с данными об операциях переводах {@link TransferOperation}
 */
@Repository
@AllArgsConstructor
public class TransferOperationDaoImpl implements OperationDao<TransferOperation> {

    private final String SAVE_TRANSFER_OPERATION =
            "INSERT INTO transfer_operations(id, from_user_id, to_user_id, from_account_id, to_account_id," +
                    " date_of_creation, funds, from_account_currency) values(DEFAULT, ?, ?, ?, ?, ?, ?, ?)";

    private final String DELETE_TRANSFER_OPERATION_BY_ID =
            "DELETE FROM transfer_operations WHERE id = ?";

    private final String GET_TRANSFER_OPERATION_BY_ID =
            "SELECT * FROM transfer_operations WHERE id = ?";

    private final String GET_TRANSFER_OPERATIONS_BY_FROM_USER_ID =
            "SELECT * FROM transfer_operations WHERE from_user_id = ?";

    private final String GET_TRANSFER_OPERATIONS_BY_TO_USER_ID =
            "SELECT * FROM transfer_operations WHERE to_user_id = ?";

    private final String GET_TRANSFER_OPERATIONS_BY_FROM_ACCOUNT_ID =
            "SELECT * FROM transfer_operations WHERE from_account_id = ?";

    private final String GET_TRANSFER_OPERATIONS_BY_TO_ACCOUNT_ID =
            "SELECT * FROM transfer_operations WHERE to_account_id = ?";

    private final DataSource dataSource;

    /**
     * Метод, сохраняющий данные об операциях переводах {@link TransferOperation}
     * @param operation {@link TransferOperation} - операция перевода
     * @throws SQLException - исключение, возникающее при сохранении данных об операциях перевода
     */
    @Override
    public void save(TransferOperation operation) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(SAVE_TRANSFER_OPERATION)) {
            Date date = new Date(operation.getDateOfCreation().getTime());

            prepareStatement.setInt(1, operation.getFromUserId());
            prepareStatement.setInt(2, operation.getToUserId());
            prepareStatement.setInt(3, operation.getFromAccountId());
            prepareStatement.setInt(4, operation.getToAccountId());
            prepareStatement.setDate(5, date);
            prepareStatement.setBigDecimal(6, operation.getFunds());
            prepareStatement.setShort(7, operation.getFromAccountCurrency().getNumber());

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, удаляющий данные об операции перевода {@link TransferOperation} с идентификатором равным id
     * @param id - идентификатор операции перевода
     * @throws SQLException - исключение, возникшее при удалении данных об операциях перевода
     */
    @Override
    public void deleteById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(DELETE_TRANSFER_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            prepareStatement.executeUpdate();
        }
    }

    /**
     * Метод, возвращающий данные об операции перевода {@link TransferOperation} с идентификатором равным id
     * @param id - идентификатор операции перевода
     * @return {@link Optional<TransferOperation>} - информация об операции перевода
     * @throws SQLException - исключение, возникшее при получении данных об операции перевода
     */
    @Override
    public Optional<TransferOperation> getById(Integer id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement(GET_TRANSFER_OPERATION_BY_ID)) {

            prepareStatement.setInt(1, id);

            ResultSet resultSet = prepareStatement.executeQuery();

            Optional<TransferOperation> operation = resultSet.next() ?
                    Optional.of(getTransferOperationFromResultSet(resultSet)): Optional.empty();

            resultSet.close();
            return operation;
        }
    }

    /**
     * Метод, возвращающий данные об операциях перевода {@link TransferOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<TransferOperation>} - список операций
     * @throws SQLException - исключение, возникшее при получении данных об операциях перевода
     */
    @Override
    public List<TransferOperation> getAllByAccountId(Integer accountId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement firstPrepareStatement = connection
                     .prepareStatement(GET_TRANSFER_OPERATIONS_BY_FROM_ACCOUNT_ID);
             PreparedStatement secondPrepareStatement = connection
                .prepareStatement(GET_TRANSFER_OPERATIONS_BY_TO_ACCOUNT_ID)) {

            firstPrepareStatement.setInt(1, accountId);
            secondPrepareStatement.setInt(1, accountId);

            ResultSet firstResultSet = firstPrepareStatement.executeQuery();
            ResultSet secondResultSet = secondPrepareStatement.executeQuery();
            List<TransferOperation> operations = new ArrayList<>();

            while (firstResultSet.next()) {
                operations.add(getTransferOperationFromResultSet(firstResultSet));
            }

            while (secondResultSet.next()) {
                TransferOperation operation = getTransferOperationFromResultSet(secondResultSet);
                if (operations.stream().noneMatch(x -> Objects.equals(x.getId(), operation.getId()))) {
                    operations.add(operation);
                }
            }

            firstResultSet.close();
            secondResultSet.close();
            return operations;
        }
    }

    /**
     * Метод, возвращающий данные об операциях {@link TransferOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<TransferOperation>} - список операций перевода
     * @throws SQLException - исключение, возникшее при получении данных об операциях перевода
     */
    @Override
    public List<TransferOperation> getAllByUserId(Integer userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement firstPrepareStatement = connection
                     .prepareStatement(GET_TRANSFER_OPERATIONS_BY_FROM_USER_ID);
             PreparedStatement secondPrepareStatement = connection
                     .prepareStatement(GET_TRANSFER_OPERATIONS_BY_TO_USER_ID)) {

            firstPrepareStatement.setInt(1, userId);
            secondPrepareStatement.setInt(1, userId);

            ResultSet firstResultSet = firstPrepareStatement.executeQuery();
            ResultSet secondResultSet = secondPrepareStatement.executeQuery();
            List<TransferOperation> operations = new ArrayList<>();

            while (firstResultSet.next()) {
                operations.add(getTransferOperationFromResultSet(firstResultSet));
            }

            while (secondResultSet.next()) {
                TransferOperation operation = getTransferOperationFromResultSet(secondResultSet);
                if (operations.stream().noneMatch(x -> Objects.equals(x.getId(), operation.getId()))) {
                    operations.add(operation);
                }
            }

            firstResultSet.close();
            secondResultSet.close();
            return operations;
        }
    }

    /**
     * Метод, получающий данные об операции перевода {@link TransferOperation} из ResultSet
     * @param resultSet {@link ResultSet} - объект, для чтения данных о счетах из БД
     * @return {@link TransferOperation} информация об операции перевода
     * @throws SQLException - исключение, возникшее при чтении данных из БД об операции перевода
     */
    private TransferOperation getTransferOperationFromResultSet(ResultSet resultSet) throws SQLException {
        return TransferOperation.builder()
                .id(resultSet.getInt("id"))
                .fromUserId(resultSet.getInt("from_user_id"))
                .toUserId(resultSet.getInt("to_user_id"))
                .fromAccountId(resultSet.getInt("from_account_id"))
                .toAccountId(resultSet.getInt("to_account_id"))
                .dateOfCreation(resultSet.getDate("date_of_creation"))
                .funds(resultSet.getBigDecimal("funds"))
                .fromAccountCurrency(Currency.getByNumber(resultSet.getShort("from_account_currency")))
                .build();
    }

}
