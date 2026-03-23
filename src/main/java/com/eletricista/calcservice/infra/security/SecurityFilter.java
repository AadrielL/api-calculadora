package com.eletricista.calcservice.infra.security; // Ajuste o package conforme o projeto

import com.eletricista.calcservice.infra.security.service.TokenService;
import com.eletricista.calcservice.infra.security.tenant.TenantContext; //
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService; //

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null) {
            var login = tokenService.validateToken(token); // Valida se o token é legítimo

            if (!login.isEmpty()) {
                var tenantId = tokenService.getTenantIdFromToken(token); // Extrai o Tenant do Token

                // Define o Tenant no Contexto (Substitui o antigo Interceptor)
                TenantContext.setCurrentTenant(tenantId);

                // Autentica o usuário no Spring Security
                var authentication = new UsernamePasswordAuthenticationToken(login, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Garante que o Tenant seja limpo após a resposta
            TenantContext.clear();
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }
}