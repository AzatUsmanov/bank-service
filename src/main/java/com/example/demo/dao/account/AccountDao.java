package com.example.demo.dao.account;

import com.example.demo.domain.dto.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDao {

    void save(Account account);

    void deleteById(Integer id);

    void updateById(Integer id, Account account);

    Optional<Account> getById(Integer id);

    List<Account> getByUserId(Integer userId);

}
