package com.example.demo.service.authentication;

import com.example.demo.domain.dto.authentication.JwtAuthenticationResponse;
import com.example.demo.domain.dto.authentication.SignInRequest;
import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Класс, выполняющий логику регистрации и аутентификации пользователя
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    public AuthenticationServiceImpl(@Qualifier("UserServiceImpl") UserService userService,
                                     JwtService jwtService,
                                     PasswordEncoder passwordEncoder,
                                     AuthenticationManager authenticationManager,
                                     UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Метод регистрирующий пользователя и возвращающий токен аутентификации
     * @param request - запрос на регистрацию
     * @return {@link JwtAuthenticationResponse} - токен аутентификации
     * @throws NotUniqueEmailException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальной почтой
     * @throws NotUniqueUsernameException - исключение, возникающее при попытке зарегистрировать пользователя с неуникальным именем
     */
    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) throws NotUniqueEmailException, NotUniqueUsernameException {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authorities(List.of(Authority.ADMIN_VIEW, Authority.USER_EDIT))
                .build();

        userService.create(user);

        String jwt = jwtService.generateToken(user);

        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Метод, аутентифицирующий пользователя и возвращающий токен аутентификации
     * @param request - запрос на аутентификацию
     * @return {@link JwtAuthenticationResponse} - токен аутентификации
     */
    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserDetails userDetails = userDetailsService
                .loadUserByUsername(request.getUsername());

        String jwt = jwtService.generateToken(userDetails);

        return new JwtAuthenticationResponse(jwt);
    }


}
