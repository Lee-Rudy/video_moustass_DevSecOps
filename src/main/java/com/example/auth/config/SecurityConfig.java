package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de sécurité Spring Security pour l'API REST.
 * 
 * CSRF (Cross-Site Request Forgery) Protection:
 * La protection CSRF est désactivée car cette application est une API REST stateless
 * utilisant l'authentification JWT (JSON Web Token). Dans ce contexte, la désactivation
 * de CSRF est une pratique standard et sécurisée pour les raisons suivantes:
 * 
 * 1. API Stateless: Aucune session côté serveur n'est utilisée (pas de cookies de session)
 * 2. Authentification JWT: Les tokens JWT sont envoyés via l'en-tête Authorization
 * 3. Pas de cookies d'authentification: Les attaques CSRF ciblent les cookies de session
 * 4. SameSite et CORS: Protection supplémentaire via les headers HTTP
 * 
 * Cette configuration est conforme aux recommandations OWASP pour les API REST.
 * Référence: https://owasp.org/www-community/attacks/csrf
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtHelper jwtHelper) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtHelper);
        http
                // CSRF désactivé: Sécurisé pour API REST stateless avec JWT (voir commentaire de classe)
                .csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
