package com.example.demo.security;

import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Класс, описывающий конфигурацию безопасности сервиса
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request

                        .requestMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/user/**").hasAuthority("ADMIN_EDIT")

                        .requestMatchers(HttpMethod.GET,"/account/**").hasAnyAuthority("USER_VIEW", "ADMIN_VIEW")
                        .requestMatchers(HttpMethod.POST,"/account/**", "/account").hasAnyAuthority("USER_EDIT", "ADMIN_EDIT")
                        .requestMatchers(HttpMethod.DELETE,"/account/**").hasAnyAuthority("USER_EDIT", "ADMIN_EDIT")
                        .requestMatchers(HttpMethod.PATCH,"/account/**").hasAnyAuthority("ADMIN_EDIT")

                        .requestMatchers(HttpMethod.GET,"/replenishment/**").hasAnyAuthority("USER_VIEW", "ADMIN_VIEW")
                        .requestMatchers(HttpMethod.POST,"/replenishment/**").hasAnyAuthority("USER_EDIT", "ADMIN_EDIT")
                        .requestMatchers(HttpMethod.DELETE,"/replenishment/**").hasAnyAuthority("ADMIN_EDIT")
                        .requestMatchers(HttpMethod.PATCH,"/replenishment/**").hasAnyAuthority("ADMIN_EDIT")

                        .requestMatchers(HttpMethod.GET,"/withdrawal/**").hasAnyAuthority("USER_VIEW", "ADMIN_VIEW")
                        .requestMatchers(HttpMethod.POST,"/withdrawal/**").hasAnyAuthority("USER_EDIT", "ADMIN_EDIT")
                        .requestMatchers(HttpMethod.DELETE,"/withdrawal/**").hasAnyAuthority("ADMIN_EDIT")
                        .requestMatchers(HttpMethod.PATCH,"/withdrawal/**").hasAnyAuthority("ADMIN_EDIT")

                        .requestMatchers(HttpMethod.GET,"/transfer/**").hasAnyAuthority("USER_VIEW", "ADMIN_VIEW")
                        .requestMatchers(HttpMethod.POST,"/transfer/**").hasAnyAuthority("USER_EDIT", "ADMIN_EDIT")
                        .requestMatchers(HttpMethod.DELETE,"/transfer/**").hasAnyAuthority("ADMIN_EDIT")
                        .requestMatchers(HttpMethod.PATCH,"/transfer/**").hasAnyAuthority("ADMIN_EDIT")

                        .anyRequest().authenticated())

                        .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
