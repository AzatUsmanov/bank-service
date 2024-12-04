package com.example.demo.service.user;

import com.example.demo.domain.model.User;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

import java.util.Optional;

public interface UserService {

    void create(User user) throws NotUniqueEmailException, NotUniqueUsernameException;

    User getById(Integer id);

    Optional<User> findByUsername(String username);

    boolean isPresentById(Integer id);
}
