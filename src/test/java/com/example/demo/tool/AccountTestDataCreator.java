package com.example.demo.tool;

import com.example.demo.domain.dto.Account;
import com.example.demo.domain.model.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class AccountTestDataCreator {

    @Autowired
    private DataSource dataSource;

    public void deleteAccountsData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM accounts")) {
            prepareStatement.executeUpdate();
        }
    }

    public Account getSavedAccount(Integer id, Integer userId) {
        Account account = generateAccount(id, userId);
        saveAccount(account);
        return account;
    }

    public Account generateAccount(Integer id, Integer userId) {
        return Account.builder()
                .id(id)
                .userId(userId)
                .dateOfCreation(Date.valueOf("2020-10-10"))
                .funds(BigDecimal.valueOf(10.00 + id))
                .currency(Currency.USD)
                .build();
    }

    public void saveAccount(Account account) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(
                     "INSERT INTO accounts(id, user_id, date_of_creation, funds, currency) values(?, ?, ?, ?, ?)")) {

            prepareStatement.setInt(1, account.getId());
            prepareStatement.setInt(2, account.getUserId());
            prepareStatement.setDate(3, new Date(account.getDateOfCreation().getTime()));
            prepareStatement.setBigDecimal(4, account.getFunds());
            prepareStatement.setShort(5, account.getCurrency().getNumber());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
