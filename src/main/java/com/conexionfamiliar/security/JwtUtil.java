package com.conexionfamiliar.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import com.conexionfamiliar.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;


import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setClaims(Map.of("id", user.getId(), "role", user.getRole()))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmail(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public boolean validateToken(String authToken) {
        return false;
    }

    public String getUsernameFromToken(String authToken) {
        return null;
    }

    public Collection<? extends GrantedAuthority> getRolesFromToken(String authToken) {
        return null;
    }
}
