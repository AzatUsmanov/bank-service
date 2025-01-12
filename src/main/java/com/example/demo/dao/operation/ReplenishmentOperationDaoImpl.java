package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.model.User;

import lombok.AllArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    private final RowMapper<ReplenishmentOperation> operationRowMapper = (rs, rowNum) -> ReplenishmentOperation.builder()
            .id(rs.getInt("id"))
            .userId(rs.getInt("user_id"))
            .accountId(rs.getInt("account_id"))
            .dateOfCreation(rs.getDate("date_of_creation"))
            .funds(rs.getBigDecimal("funds"))
            .currency(Currency.valueOf(rs.getShort("currency")))
            .build();

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод, сохраняющий данные об операциях пополнения {@link ReplenishmentOperation}
     * @param operation {@link ReplenishmentOperation} - операция пополнения
     */
    @Override
    public void save(ReplenishmentOperation operation) {
        jdbcTemplate.update(SAVE_REPLENISHMENT_OPERATION,
                operation.getUserId(),
                operation.getAccountId(),
                operation.getDateOfCreation(),
                operation.getFunds(),
                operation.getCurrency().getNumber());
    }

    /**
     * Метод, удаляющий данные об операции пополнения {@link ReplenishmentOperation} с идентификатором равным id
     * @param id - идентификатор операции пополнения
     */
    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_REPLENISHMENT_OPERATION_BY_ID, id);
    }

    /**
     * Метод, возвращающий данные об операции пополнения {@link ReplenishmentOperation} с идентификатором равным id
     * @param id - идентификатор операции пополнения
     * @return {@link ReplenishmentOperation} - информация об операции пополнении
     */
    @Override
    public Optional<ReplenishmentOperation> getById(Integer id) {
        List<ReplenishmentOperation> operations = jdbcTemplate.query(GET_REPLENISHMENT_OPERATION_BY_ID, operationRowMapper, id);
        return operations.isEmpty() ? Optional.empty() : Optional.of(operations.getFirst());
    }

    /**
     * Метод, возвращающий данные об операциях пополнения {@link ReplenishmentOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<ReplenishmentOperation>} - список операций
     */
    @Override
    public List<ReplenishmentOperation> getAllByAccountId(Integer accountId) {
        return jdbcTemplate.query(GET_REPLENISHMENT_OPERATIONS_BY_ACCOUNT_ID, operationRowMapper, accountId);
    }

    /**
     * Метод, возвращающий данные об операциях {@link ReplenishmentOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<ReplenishmentOperation>} - список операций
     */
    @Override
    public List<ReplenishmentOperation> getAllByUserId(Integer userId) {
        return jdbcTemplate.query(GET_REPLENISHMENT_OPERATIONS_BY_USER_ID, operationRowMapper, userId);
    }

}
