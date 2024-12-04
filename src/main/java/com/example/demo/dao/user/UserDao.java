package com.example.demo.dao.user;

import com.example.demo.domain.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {

    void save(User user) throws SQLException;

    Optional<User> getById(Integer id) throws SQLException;

    Optional<User> getByUsername(String mail) throws SQLException;

    Optional<User> getByMail(String mail) throws SQLException;

}
