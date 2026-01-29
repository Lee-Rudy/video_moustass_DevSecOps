package com.example.auth.oauth2.controller;

import com.example.auth.config.JwtHelper;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.mfa.service.MfaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour la gestion de l'authentification OAuth2 et MFA
 */
@RestController
@RequestMapping("/api/oauth2")
@CrossOrigin(origins = "${app.frontend.origin}")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    private final MfaService mfaService;
    private final SpringDataUsersRepository usersRepository;
    private final JwtHelper jwtHelper;

    public OAuth2Controller(
            MfaService mfaService,
            SpringDataUsersRepository usersRepository,
            JwtHelper jwtHelper) {
        this.mfaService = mfaService;
        this.usersRepository = usersRepository;
        this.jwtHelper = jwtHelper;
    }

    /**
     * Vérifie le code MFA et retourne un token JWT si valide
     * 
     * POST /api/oauth2/verify-mfa
     * Body: { "sessionToken": "...", "code": "123456" }
     * 
     * @return JWT token + informations utilisateur
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfaCode(@RequestBody MfaVerificationRequest request) {
        logger.info("Vérification du code MFA pour session: {}", request.sessionToken());

        // Validation des paramètres
        if (request.sessionToken() == null || request.sessionToken().isBlank()) {
            logger.warn("Session token manquant");
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Session token requis"));
        }

        if (request.code() == null || request.code().isBlank()) {
            logger.warn("Code MFA manquant");
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Code MFA requis"));
        }

        // Valide le code MFA
        Integer userId = mfaService.validateMfaCode(request.sessionToken(), request.code());

        if (userId == null) {
            logger.warn("Code MFA invalide ou expiré pour session: {}", request.sessionToken());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Code MFA invalide ou expiré"));
        }

        // Récupère l'utilisateur
        var userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.error("Utilisateur introuvable avec ID: {}", userId);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des informations utilisateur"));
        }

        UsersJpaEntity user = userOpt.get();

        // Génère un token JWT
        String token = jwtHelper.createToken(user.getId());

        logger.info("Authentification OAuth2 + MFA réussie pour utilisateur ID: {}, Email: {}", 
            user.getId(), user.getMail());

        // Retourne le token et les informations utilisateur
        return ResponseEntity.ok(Map.of(
            "token", token,
            "userId", user.getId(),
            "name", user.getName(),
            "email", user.getMail(),
            "isAdmin", user.isAdmin(),
            "message", "Authentification réussie"
        ));
    }

    /**
     * Renvoie un nouveau code MFA
     * 
     * POST /api/oauth2/resend-mfa
     * Body: { "sessionToken": "..." }
     * 
     * @return Nouveau session token
     */
    @PostMapping("/resend-mfa")
    public ResponseEntity<?> resendMfaCode(@RequestBody ResendMfaRequest request) {
        logger.info("Demande de renvoi du code MFA pour session: {}", request.sessionToken());

        // Validation du paramètre
        if (request.sessionToken() == null || request.sessionToken().isBlank()) {
            logger.warn("Session token manquant");
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Session token requis"));
        }

        // Récupère l'ancien code MFA pour trouver l'utilisateur
        var mfaCode = mfaService.getMfaCodeBySessionToken(request.sessionToken());
        if (mfaCode == null) {
            logger.warn("Session MFA introuvable: {}", request.sessionToken());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Session MFA introuvable"));
        }

        // Récupère l'utilisateur
        var userOpt = usersRepository.findById(mfaCode.getUserId());
        if (userOpt.isEmpty()) {
            logger.error("Utilisateur introuvable avec ID: {}", mfaCode.getUserId());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des informations utilisateur"));
        }

        UsersJpaEntity user = userOpt.get();

        // Génère et envoie un nouveau code MFA
        try {
            String newSessionToken = mfaService.createAndSendMfaCode(
                user.getId(),
                user.getMail(),
                user.getName()
            );

            logger.info("Nouveau code MFA généré et envoyé pour utilisateur ID: {}", user.getId());

            return ResponseEntity.ok(Map.of(
                "sessionToken", newSessionToken,
                "message", "Nouveau code MFA envoyé avec succès"
            ));
        } catch (Exception e) {
            logger.error("Erreur lors du renvoi du code MFA: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Échec de l'envoi du code MFA"));
        }
    }

    /**
     * Record pour la requête de vérification MFA
     */
    private record MfaVerificationRequest(String sessionToken, String code) {}

    /**
     * Record pour la requête de renvoi MFA
     */
    private record ResendMfaRequest(String sessionToken) {}
}
