package com.example.demo.tool;

import com.example.demo.controller.AuthenticationController;
import com.example.demo.domain.dto.authentication.SignInRequest;
import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.authority.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class UserTestDataCreator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void deleteUserDataBaseData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM users")) {
            prepareStatement.executeUpdate();
        }
    }

    public void deleteAuthoritiesDataBaseData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM authorities")) {
            prepareStatement.executeUpdate();
        }
    }

    public String getAuthenticationToken(User user) {
        return authenticationController
                .signIn(new SignInRequest(user.getUsername(), user.getPassword()))
                .getToken();
    }

    public User getRegisteredUserWithUserAuthoritiesById(Integer id) {
        User user = generate(id);
        user.setAuthorities(List.of(Authority.USER_VIEW, Authority.USER_EDIT));
        User userWithEncodedPassword = getUserCopyWithEncodedPassword(user);
        save(userWithEncodedPassword);
        saveAuthoritiesFromUser(user);
        return user;
    }

    public User getRegisteredUserWithAdminAuthoritiesById(Integer id) {
        User user = generate(id);
        user.setAuthorities(List.of(Authority.ADMIN_VIEW, Authority.ADMIN_EDIT));
        User userWithEncodedPassword = getUserCopyWithEncodedPassword(user);
        save(userWithEncodedPassword);
        saveAuthoritiesFromUser(user);
        return user;
    }

    public User generate(Integer id) {
        return User.builder()
                .id(id)
                .username(id + "username")
                .email(id + "mail@mail.com")
                .password(id + "password")
                .authorities(List.of(Authority.USER_VIEW, Authority.USER_EDIT))
                .build();
    }

    public void save(User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepareStatement = connection.prepareStatement(
                     "INSERT INTO users(id, username, email, password) values(?, ?, ?, ?)")) {

            prepareStatement.setInt(1, user.getId());
            prepareStatement.setString(2, user.getUsername());
            prepareStatement.setString(3, user.getEmail());
            prepareStatement.setString(4, user.getPassword());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAuthoritiesFromUser(User user) {
        authorityService.saveByUserId(user.getUserAuthorities(), user.getId());
    }

    public User getUserCopyWithEncodedPassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(encodedPassword)
                .build();
    }

}
