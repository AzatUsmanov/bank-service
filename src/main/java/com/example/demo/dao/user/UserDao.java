package com.example.demo.dao.user;

import com.example.demo.domain.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {

    void save(User user);

    Optional<User> getById(Integer id);

    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

}
