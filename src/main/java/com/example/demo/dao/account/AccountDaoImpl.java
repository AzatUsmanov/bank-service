package com.example.demo.dao.account;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
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
import java.util.Arrays;
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
            "UPDATE accounts SET funds = ?, currency = ? WHERE id = ?";

    private final String GET_ACCOUNT_BY_ID =
            "SELECT * FROM accounts WHERE id = ?";

    private final String GET_ACCOUNTS_BY_USER_ID =
            "SELECT * FROM accounts WHERE user_id = ?";

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> Account.builder()
            .id(rs.getInt("id"))
            .userId(rs.getInt("user_id"))
            .dateOfCreation(rs.getDate("date_of_creation"))
            .funds(rs.getBigDecimal("funds"))
            .currency(Currency.valueOf(rs.getShort("currency")))
            .build();

    private final JdbcTemplate jdbcTemplate;


    /**
     * Метод, сохраняющий данные о счете {@link Account}
     * @param account - данные о счете
     */
    @Override
    public void save(Account account) {
        jdbcTemplate.update(SAVE_ACCOUNT,
                account.getUserId(),
                account.getDateOfCreation(),
                account.getFunds(),
                account.getCurrency().getNumber());
    }

    /**
     * Метод, удаляющий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     */
    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_ACCOUNT_BY_ID, id);
    }


    /**
     * Метод, обновляющий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     * @param account {@link Account} - новые данные о счете
     */
    @Override
    public void updateById(Integer id, Account account) {
        jdbcTemplate.update(UPDATE_ACCOUNT_BY_ID,
                account.getFunds(),
                account.getCurrency().getNumber(),
                id);
    }

    /**
     /**
     * Метод, возвращающий данные о счете {@link Account} с идентификатором равным id
     * @param id - идентификатор счета
     */
    @Override
    public Optional<Account> getById(Integer id) {
        List<Account> accounts = jdbcTemplate.query(GET_ACCOUNT_BY_ID, accountRowMapper, id);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.getFirst());
    }

    /**
     * Метод, возвращающий данные о счетах {@link Account}, принадлежащих пользователю {@link User} с идентификатором равным userId
     * @param userId - идентификатор пользователя
     * @return {@link List<Account>} - список счетов
     */
    @Override
    public List<Account> getByUserId(Integer userId)  {
        return jdbcTemplate.query(GET_ACCOUNTS_BY_USER_ID, accountRowMapper, userId);
    }

}
