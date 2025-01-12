package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.model.User;

import lombok.AllArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    private final RowMapper<TransferOperation> operationRowMapper = (rs, rowNum) -> TransferOperation.builder()
            .id(rs.getInt("id"))
            .fromUserId(rs.getInt("from_user_id"))
            .toUserId(rs.getInt("to_user_id"))
            .fromAccountId(rs.getInt("from_account_id"))
            .toAccountId(rs.getInt("to_account_id"))
            .dateOfCreation(rs.getDate("date_of_creation"))
            .funds(rs.getBigDecimal("funds"))
            .fromAccountCurrency(Currency.valueOf(rs.getShort("from_account_currency")))
            .build();

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод, сохраняющий данные об операциях переводах {@link TransferOperation}
     * @param operation {@link TransferOperation} - операция перевода
     */
    @Override
    public void save(TransferOperation operation) {
        jdbcTemplate.update(SAVE_TRANSFER_OPERATION,
                operation.getFromUserId(),
                operation.getToUserId(),
                operation.getFromAccountId(),
                operation.getToAccountId(),
                operation.getDateOfCreation(),
                operation.getFunds(),
                operation.getFromAccountCurrency().getNumber());
    }

    /**
     * Метод, удаляющий данные об операции перевода {@link TransferOperation} с идентификатором равным id
     * @param id - идентификатор операции перевода
     */
    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_TRANSFER_OPERATION_BY_ID, id);
    }

    /**
     * Метод, возвращающий данные об операции перевода {@link TransferOperation} с идентификатором равным id
     * @param id - идентификатор операции перевода
     * @return {@link TransferOperation>=} - информация об операции перевода
     */
    @Override
    public Optional<TransferOperation> getById(Integer id) {
        List<TransferOperation> operations = jdbcTemplate.query(GET_TRANSFER_OPERATION_BY_ID, operationRowMapper, id);
        return operations.isEmpty() ? Optional.empty() : Optional.of(operations.getFirst());
    }

    /**
     * Метод, возвращающий данные об операциях перевода {@link TransferOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<TransferOperation>} - список операций
     */
    @Override
    public List<TransferOperation> getAllByAccountId(Integer accountId) {
        List<TransferOperation> operations = new ArrayList<>();
        operations.addAll(jdbcTemplate.query(GET_TRANSFER_OPERATIONS_BY_FROM_ACCOUNT_ID, operationRowMapper, accountId));
        operations.addAll(jdbcTemplate.query(GET_TRANSFER_OPERATIONS_BY_TO_ACCOUNT_ID, operationRowMapper, accountId));
        return operations;
    }

    /**
     * Метод, возвращающий данные об операциях {@link TransferOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<TransferOperation>} - список операций перевода
     */
    @Override
    public List<TransferOperation> getAllByUserId(Integer userId) {
        Set<TransferOperation> operations = new HashSet<>(
                jdbcTemplate.query(GET_TRANSFER_OPERATIONS_BY_FROM_USER_ID, operationRowMapper, userId));
        jdbcTemplate
                .query(GET_TRANSFER_OPERATIONS_BY_TO_USER_ID, operationRowMapper, userId)
                .stream()
                .filter(x -> operations.stream().noneMatch(y -> Objects.equals(y.getId(), x.getId())))
                .forEach(operations::add);

        return operations.stream().toList();
    }



}
