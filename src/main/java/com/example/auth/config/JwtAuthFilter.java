package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT : pour /api/users, /api/orders* et /api/logs, exige un Bearer valide et définit request.setAttribute("userId", id).
 * Pour /api/login et /api/inscription, laisse passer sans JWT.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;

    public JwtAuthFilter(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Laisser passer les requêtes OPTIONS (preflight CORS) sans JWT
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/login") || path.startsWith("/api/inscription")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!path.startsWith("/api/users") && !path.startsWith("/api/orders") && !path.startsWith("/api/logs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token manquant ou invalide\"}");
            return;
        }
        if (token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token manquant\"}");
            return;
        }

        try {
            Integer userId = jwtHelper.parseUserId(token);
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token invalide ou expiré\"}");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        String alt = request.getHeader("X-Auth-Token");
        if (alt != null && !alt.isBlank()) {
            return alt.trim();
        }
        return null;
    }
}
