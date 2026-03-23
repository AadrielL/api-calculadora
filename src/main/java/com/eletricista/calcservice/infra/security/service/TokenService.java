package com.eletricista.calcservice.infra.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject(); // Retorna o login/email do usuário (Subject)
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    /**
     * Extrai o tenantId (ID do profissional) de dentro do Payload do JWT.
     * Vital para o funcionamento do Multitenancy.
     */
    public String getTenantIdFromToken(String token) {
        try {
            return JWT.decode(token).getClaim("tenantId").asString();
        } catch (Exception e) {
            return null;
        }
    }
}