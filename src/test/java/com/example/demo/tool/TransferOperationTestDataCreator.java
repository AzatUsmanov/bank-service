package com.example.demo.tool;

import com.example.demo.domain.dto.operation.TransferOperation;
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
public class TransferOperationTestDataCreator {

    @Autowired
    private DataSource dataSource;

    public void deleteDataBaseData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM replenishment_operations")) {
            prepareStatement.executeUpdate();
        }
    }

    public TransferOperation getSavedOperation(
            Integer id, Integer fromAccountId, Integer toAccountId, Integer fromUserId, Integer toUserId) {
        TransferOperation operation = generateOperation(id, fromAccountId, toAccountId, fromUserId, toUserId);
        saveOperation(operation);
        return operation;
    }

    public TransferOperation generateOperation(
            Integer id, Integer fromAccountId, Integer toAccountId, Integer fromUserId, Integer toUserId) {
        return TransferOperation.builder()
                .id(id)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .dateOfCreation(Date.valueOf("2020-10-10"))
                .funds(BigDecimal.valueOf(5 + id))
                .fromAccountCurrency(Currency.USD)
                .build();
    }

    public void saveOperation(TransferOperation operation) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection
                     .prepareStatement("INSERT INTO transfer_operations" +
                             "(id, from_user_id, to_user_id, from_account_id, to_account_id," +
                             " date_of_creation, funds, from_account_currency) values(?, ?, ?, ?, ?, ?, ?, ?)")) {
            Date date = new Date(operation.getDateOfCreation().getTime());

            prepareStatement.setInt(1, operation.getId());
            prepareStatement.setInt(2, operation.getFromUserId());
            prepareStatement.setInt(3, operation.getToUserId());
            prepareStatement.setInt(4, operation.getFromAccountId());
            prepareStatement.setInt(5, operation.getToAccountId());
            prepareStatement.setDate(6, date);
            prepareStatement.setBigDecimal(7, operation.getFunds());
            prepareStatement.setShort(8, operation.getFromAccountCurrency().getNumber());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
