package com.example.demo.service.user;

import com.example.demo.domain.model.User;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

import java.util.Optional;

public interface UserService {

    void save(User user) throws NotUniqueEmailException, NotUniqueUsernameException;

    User getById(Integer id);

    User getByUsername(String username);

    boolean isPresentById(Integer id);
}
