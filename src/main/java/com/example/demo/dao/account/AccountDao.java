package com.example.demo.dao.account;

import com.example.demo.domain.dto.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDao {

    void save(Account account) throws SQLException;

    void deleteById(Integer id) throws SQLException;

    void updateById(Integer id, Account account) throws SQLException;

    Optional<Account> getById(Integer id) throws SQLException;

    List<Account> getByUserId(Integer userId) throws SQLException;

}
