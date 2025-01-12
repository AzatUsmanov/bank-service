package com.example.demo.dao.operation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OperationDao <T> {

    void save(T operation);

    void deleteById(Integer id);

    Optional<T> getById(Integer id);

    List<T> getAllByAccountId(Integer accountId);

    List<T> getAllByUserId(Integer userId);

}
