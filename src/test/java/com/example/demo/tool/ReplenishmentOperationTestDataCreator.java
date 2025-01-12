package com.example.demo.tool;

import com.example.demo.domain.dto.operation.ReplenishmentOperation;
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
public class ReplenishmentOperationTestDataCreator {

    @Autowired
    private DataSource dataSource;

    public void deleteDataBaseData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM replenishment_operations")) {
            prepareStatement.executeUpdate();
        }
    }

    public ReplenishmentOperation getSavedOperation(Integer id, Integer accountId, Integer userId) {
        ReplenishmentOperation operation = generateReplenishmentOperation(id, accountId, userId);
        saveOperation(operation);
        return operation;
    }

    public ReplenishmentOperation generateReplenishmentOperation(
            Integer id, Integer accountId, Integer userId) {
        return ReplenishmentOperation.builder()
                .id(id)
                .userId(userId)
                .accountId(accountId)
                .dateOfCreation(Date.valueOf("2020-10-10"))
                .funds(BigDecimal.valueOf(5.00 + id))
                .currency(Currency.USD)
                .build();
    }

    public void saveOperation(ReplenishmentOperation operation) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement("INSERT INTO replenishment_operations" +
                             "(id, user_id, account_id, date_of_creation, funds, currency) values(?, ?, ?, ?, ?, ?)")) {
            Date date = new Date(operation.getDateOfCreation().getTime());

            prepareStatement.setInt(1, operation.getId());
            prepareStatement.setInt(2, operation.getUserId());
            prepareStatement.setInt(3, operation.getAccountId());
            prepareStatement.setDate(4, date);
            prepareStatement.setBigDecimal(5, operation.getFunds());
            prepareStatement.setShort(6, operation.getCurrency().getNumber());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
