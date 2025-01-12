package com.example.demo.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Класс, описывающий информацию о пользователе
 */
@Data
@Builder
@AllArgsConstructor
public class User implements UserDetails {

    public final static int MIN_USERNAME_LENGTH = 5;

    public final static int MAX_USERNAME_LENGTH = 30;

    public final static int MIN_PASSWORD_LENGTH = 5;

    public final static int MAX_PASSWORD_LENGTH = 30;

    @Schema(description = "идентификатор", example = "42")
    private Integer id;

    @Schema(description = "Имя пользователя", example = "username")
    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, message = "The length of the name must be between " +
            MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH)
    private String username;

    @Schema(description = "Адрес электронной почты", example = "mail@mail.ru")
    @Email(message = "The mail format is not respected")
    private String email;

    @Schema(description = "Пароль", example = "password")
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH, message = "The length of the name must be between " +
            MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH)
    private String password;

    @Schema(description = "Список полномочий", example = "[ 'USER_VIEW', 'USER_EDIT']")
    private List<Authority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
                .stream()
                .map(x -> new SimpleGrantedAuthority(x.name()))
                .toList();
    }

    public List<Authority> getUserAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", authorities=" + authorities +
                ", email='" + email + '\'' +
                " username='" + username + '\'' +
                '}';
    }
}
