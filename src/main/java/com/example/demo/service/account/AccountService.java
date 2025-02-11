package com.example.demo.service.account;

import com.example.demo.domain.dto.Account;

import java.util.List;

public interface AccountService {

    void save(Account account);

    void updateById(Integer id, Account account);

    void deleteById(Integer id);

    Account getById(Integer id);

    List<Account> getByUserId(Integer userId);

    boolean isPresentById(Integer id);

}
