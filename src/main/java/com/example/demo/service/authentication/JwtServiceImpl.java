package com.example.demo.service.authentication;

import com.example.demo.domain.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Класс, реализующий функционал по работе с токенами аутентификации
 */
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;

    /**
     * Метод, генерирующий токен аутентификации
     * @param userDetails - информация о пользователе
     * @return токен аутентификации
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Date issuedDate = new Date(System.currentTimeMillis());
        Date expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());

        return Jwts.builder()
                .claims(getClaims(userDetails))
                .subject(userDetails.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Метод, генерирующий данные для токена
     * @param userDetails - данные о пользователе
     * @return данные для генерации токена, содержащие идентификатор, имя и полномочия пользователя
     */
    @Override
    public Map<String, Object> getClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("id", user.getId());
            claims.put("email", user.getEmail());
            claims.put("authorities", userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }
        return claims;
    }

    /**
     * Метод, проверяющий валидность токена
     * @param token - токен для проверки
     * @param userDetails - информация о пользователя
     * @return true если токен валиден и false, если нет
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Метод, получающий имя пользователя из токен
     * @param token - токен для получения информации об имени пользователя
     * @return имя пользователя, полученное из токена
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Метод, проверяющий токен на истечение срока действия
     * @param token - токен для проверки
     * @return true, если срок действия истек и false, если нет
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Метод, получающий срок истечения действия
     * @param token - токен для получения информации о сроке истечения действия
     * @return дату истечения срока
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Метод, получающий информацию из токена
     * @param token - токен для получения информации
     * @param claimsResolvers - функция для получения параметра из токена
     * @return параметр
     * @param <T> - тип параметра
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Метод, получающий всю информацию из токена
     * @param token - токен для получения информации
     * @return - полученная информация
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Метод, возвращающий ключ для шифрования токена
     * @return ключ шифрования
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
