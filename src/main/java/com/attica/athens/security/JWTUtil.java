package com.attica.athens.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                SIG.HS256.key().build().getAlgorithm());
    }

    public String getId(String token) { // secretKey로 검증

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("id", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .getExpiration().before(new Date());
    }

    public String createJwt(String id, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("id", id)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
