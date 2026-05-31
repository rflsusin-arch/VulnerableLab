package br.unipar.frameworks.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Le a chave secreta do application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Le o tempo de expiração do application.properties (86400000ms = 24h)
    @Value("${jwt.expiration}")
    private long expiration;

    // Converte a string secret em uma chave criptográfica HMAC
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Gera um token JWT com email e role do usuário
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)               // quem é o usuário
                .claim("role", role)          // qual o papel dele
                .issuedAt(new Date())         // quando foi gerado
                .expiration(new Date(System.currentTimeMillis() + expiration)) // quando expira
                .signWith(getKey())           // assina com a chave secreta
                .compact();
    }

    // Extrai o email do token
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getKey()).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Extrai a role do token
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getKey()).build()
                .parseSignedClaims(token)
                .getPayload().get("role", String.class);
    }

    // Verifica se o token é válido (assinatura correta e não expirado)
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}