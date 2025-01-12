package com.example.demo.security;

import com.example.demo.dao.user.UserDao;
import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;
import com.example.demo.service.authority.AuthorityService;

import lombok.AllArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


/**
 * Класс, реализующий функционал по получению аутентификационной информации о пользователе
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userDao;

    private final AuthorityService authorityService;

    /**
     * Метод, возвращающий аутентификационную информацию о пользователе
     * @param username - имя пользователя
     * @return {@link UserDetails} - информация о пользователе
     * @throws UsernameNotFoundException - исключение, возникающее, если пользователь с таким именем не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user= userDao.getByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("There is no user with this name"));
        List<Authority> authorities = authorityService.getByUserId(user.getId());
        user.setAuthorities(authorities);
        return user;
    }

}
