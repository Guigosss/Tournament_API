package com.technofuturtic.tournament_api.api.utils;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    private final long accessTokenValidity; // 15 minutes
    private final long refreshTokenValidity; // 7 days

    public JwtUtils(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.accessTokenValidity}") long accessTokenValidity,
            @Value("${jwt.refreshTokenValidity}") long refreshTokenValidity
    ) {
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;

        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateToken(UserEntity user) {

        return Jwts.builder().signWith(secretKey)
                .claim("type", "access")
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity * 1000))
                .compact();
    }

    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Integer getId(String token) {
        return parseToken(token).get("id", Integer.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public UserContext getUser(String token) {
        Claims claims = parseToken(token);
        return new UserContext(
                claims.get("id", Integer.class),
                claims.getSubject(),
                claims.get("role", String.class)
        );
    }

    public boolean validateToken(String token) {
        return validateType(token, "access");
    }

    public String generateRefreshToken(UserEntity user) {
        return Jwts.builder().signWith(secretKey)
                .claim("type", "refresh")
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity * 1000))
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        return validateType(token, "refresh");
    }

    private boolean validateType(String token, String type) {
        try {
            Claims claims = parseToken(token);
            Date now = new Date();
            return type.equals(claims.get("type", String.class))
                    && claims.getIssuedAt() != null && !now.before(claims.getIssuedAt())
                    && claims.getExpiration() != null && now.before(claims.getExpiration())
                    && claims.get("id", Integer.class) != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
