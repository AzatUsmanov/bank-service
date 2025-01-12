package com.example.demo.service.authentication;

import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Класс, выполняющий логику регистрации и аутентификации пользователя
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceImpl(@Qualifier("UserServiceImpl") UserService userService,
                                     AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Метод регистрирующий пользователя и возвращающий токен аутентификации
     * @param request - запрос на регистрацию
     * @throws NotUniqueEmailException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальной почтой
     * @throws NotUniqueUsernameException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальным именем
     */
    @Override
    public void signUp(SignUpRequest request) throws NotUniqueEmailException, NotUniqueUsernameException {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .authorities(List.of(Authority.USER_VIEW, Authority.USER_EDIT))
                .build();

        userService.save(user);
    }

}
