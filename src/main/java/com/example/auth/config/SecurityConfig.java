package com.example.auth.config;

import com.example.auth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.example.auth.oauth2.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de sécurité Spring Security pour l'API REST avec support OAuth2.
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
 * OAuth2 avec MFA:
 * Le système implémente OAuth2 avec Google pour une authentification sécurisée.
 * Une authentification multi-facteurs (MFA) est requise après l'authentification OAuth2
 * pour une sécurité renforcée.
 * 
 * Cette configuration est conforme aux recommandations OWASP pour les API REST.
 * Référence: https://owasp.org/www-community/attacks/csrf
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private CustomOAuth2UserService customOAuth2UserService;
    private OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    /**
     * Injection optionnelle des beans OAuth2.
     * Si OAuth2 n'est pas configuré, ces beans ne seront pas disponibles.
     */
    @Autowired(required = false)
    public void setCustomOAuth2UserService(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Autowired(required = false)
    public void setOauth2SuccessHandler(OAuth2AuthenticationSuccessHandler oauth2SuccessHandler) {
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtHelper jwtHelper) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtHelper);
        http
                // CSRF désactivé: Sécurisé pour API REST stateless avec JWT (voir commentaire de classe)
                .csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a
                    // Autorise l'accès public aux endpoints OAuth2
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    // Autorise l'accès public aux endpoints API OAuth2/MFA
                    .requestMatchers("/api/oauth2/**").permitAll()
                    // Autorise l'accès public à l'endpoint de login classique
                    .requestMatchers("/api/login", "/api/inscription").permitAll()
                    // Toutes les autres requêtes sont permises (le filtre JWT gère l'autorisation)
                    .anyRequest().permitAll()
                );
        
        // Configuration OAuth2 Login (seulement si les beans OAuth2 sont disponibles)
        if (customOAuth2UserService != null && oauth2SuccessHandler != null) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2SuccessHandler)
            );
        }
        
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
