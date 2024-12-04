package com.example.demo.dao.operation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OperationDao <T> {

    void save(T operation) throws SQLException;

    void deleteById(Integer id) throws SQLException;

    Optional<T> getById(Integer id) throws SQLException;

    List<T> getAllByAccountId(Integer accountId) throws SQLException;

    List<T> getAllByUserId(Integer userId) throws SQLException;

}
