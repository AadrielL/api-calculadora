package com.eletricista.calcservice.infra.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${api.security.token.secret:}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("ERRO DE SEGURANCA: A variavel de ambiente 'JWT_SECRET' nao foi configurada.");
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            System.out.println("DEBUG: Erro ao validar Token: " + exception.getMessage());
            return "";
        }
    }

    public String getTenantIdFromToken(String token) {
        try {
            // Sincronizado com a Claim do Auth (tenantId)
            return JWT.decode(token).getClaim("tenantId").asString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getPlanTypeFromToken(String token) {
        try {
            return JWT.decode(token).getClaim("planType").asString();
        } catch (Exception e) {
            return "FREE";
        }
    }
}