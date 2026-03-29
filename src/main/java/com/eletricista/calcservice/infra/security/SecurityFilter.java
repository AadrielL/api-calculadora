package com.eletricista.calcservice.infra.security;

import com.eletricista.calcservice.infra.security.service.TokenService;
import com.eletricista.calcservice.infra.security.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // IMPORTANTE
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections; // IMPORTANTE

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null) {
            var login = tokenService.validateToken(token);

            if (login != null && !login.isEmpty()) {
                String tenantId = tokenService.getTenantIdFromToken(token);

                // Log para você conferir no console do Java se o Tenant está chegando
                System.out.println("DEBUG: Login: " + login + " | Tenant: " + tenantId);

                if (tenantId != null) {
                    TenantContext.setCurrentTenant(tenantId);
                }

                // --- A CORREÇÃO ESTÁ AQUI ---
                // Criamos uma autoridade padrão (ROLE_USER) para o Spring liberar o acesso
                var authority = new SimpleGrantedAuthority("ROLE_USER");

                var authentication = new UsernamePasswordAuthenticationToken(
                        login,
                        null,
                        Collections.singletonList(authority) // Nunca passe null aqui!
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpa o TenantId da Thread atual
            TenantContext.clear();
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }
}