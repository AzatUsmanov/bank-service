package com.example.demo.domain.dto.authentication;


import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

import static com.example.demo.domain.model.User.MAX_PASSWORD_LENGTH;
import static com.example.demo.domain.model.User.MAX_USERNAME_LENGTH;
import static com.example.demo.domain.model.User.MIN_PASSWORD_LENGTH;
import static com.example.demo.domain.model.User.MIN_USERNAME_LENGTH;

/**
 * Класс, описывающий запрос на регистрацию
 */
@Data
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию")
public class SignUpRequest {

    @Schema(description = "Имя пользователя", example = "username")
    @Size(min = 5, max = 20, message = "The length of the name must be between " +
            MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH)
    private String username;

    @Schema(description = "Адрес электронной почты", example = "mail@mail.ru")
    @Email(message = "The mail format is not respected")
    private String email;

    @Schema(description = "Пароль", example = "password")
    @Size(min = 5, max = 20, message = "The length of the name must be between " +
            MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH)
    private String password;

}
