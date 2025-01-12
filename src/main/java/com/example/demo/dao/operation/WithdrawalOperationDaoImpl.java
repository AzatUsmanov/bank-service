package com.example.demo.dao.operation;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
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


    private final RowMapper<WithdrawalOperation> operationRowMapper = (rs, rowNum) -> {
        return WithdrawalOperation.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .accountId(rs.getInt("account_id"))
                .dateOfCreation(rs.getDate("date_of_creation"))
                .funds(rs.getBigDecimal("funds"))
                .currency(Currency.valueOf(rs.getShort("currency")))
                .build();
    };

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод, сохраняющий данные об операциях списывания {@link WithdrawalOperation}
     * @param operation {@link WithdrawalOperation} - операция списывания
     */
    @Override
    public void save(WithdrawalOperation operation) {
        jdbcTemplate.update(SAVE_WITHDRAWAL_OPERATION,
                operation.getUserId(),
                operation.getAccountId(),
                operation.getDateOfCreation(),
                operation.getFunds(),
                operation.getCurrency().getNumber());
    }

    /**
     * Метод, удаляющий данные об операции списывания {@link WithdrawalOperation} с идентификатором равным id
     * @param id - идентификатор операции списывания
     */
    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_WITHDRAWAL_OPERATION_BY_ID, id);
    }

    /**
     * Метод, возвращающий данные об операции списывания {@link WithdrawalOperation} с идентификатором равным id
     * @param id - идентификатор операции списывания
     */
    @Override
    public Optional<WithdrawalOperation> getById(Integer id) {
        List<WithdrawalOperation> operations = jdbcTemplate.query(GET_WITHDRAWAL_OPERATION_BY_ID, operationRowMapper, id);
        return operations.isEmpty() ? Optional.empty() : Optional.of(operations.getFirst());
    }

    /**
     * Метод, возвращающий данные об операциях списывания {@link WithdrawalOperation}, на счет {@link Account} с идентификатором равным accountId
     * @param accountId - идентификатор счета
     * @return {@link List<WithdrawalOperation>} - список операций
     */
    @Override
    public List<WithdrawalOperation> getAllByAccountId(Integer accountId) {
        return jdbcTemplate.query(GET_WITHDRAWAL_OPERATIONS_BY_ACCOUNT_ID, operationRowMapper, accountId);
    }

    /**
     * Метод, возвращающий данные об операциях {@link WithdrawalOperation}, совершенных пользователем {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<WithdrawalOperation>} - список операций списывания
     */
    @Override
    public List<WithdrawalOperation> getAllByUserId(Integer userId) {
        return jdbcTemplate.query(GET_WITHDRAWAL_OPERATIONS_BY_USER_ID, operationRowMapper, userId);
    }

}
