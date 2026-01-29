package com.example.auth.mfa.service;

import com.example.auth.mfa.entity.MfaCodeJpaEntity;
import com.example.auth.mfa.repository.MfaCodeRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service de gestion de l'authentification multi-facteurs (MFA)
 * Génère et valide les codes MFA pour l'authentification OAuth2
 */
@Service
public class MfaService {

    private static final Logger logger = LoggerFactory.getLogger(MfaService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MfaCodeRepository mfaCodeRepository;
    private final EmailService emailService;

    @Value("${mfa.code.expiration.minutes:10}")
    private int mfaCodeExpirationMinutes;

    @Value("${mfa.code.length:6}")
    private int mfaCodeLength;

    public MfaService(MfaCodeRepository mfaCodeRepository, EmailService emailService) {
        this.mfaCodeRepository = mfaCodeRepository;
        this.emailService = emailService;
    }

    /**
     * Génère un code MFA aléatoire à N chiffres
     * 
     * @return Code MFA sous forme de String
     */
    private String generateMfaCode() {
        int maxValue = (int) Math.pow(10, mfaCodeLength);
        int code = SECURE_RANDOM.nextInt(maxValue);
        return String.format("%0" + mfaCodeLength + "d", code);
    }

    /**
     * Génère un token de session unique
     * 
     * @return Token de session UUID
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Crée et envoie un code MFA pour un utilisateur
     * 
     * @param userId ID de l'utilisateur
     * @param userEmail Email de l'utilisateur
     * @param userName Nom de l'utilisateur
     * @return Token de session associé au code MFA
     * @throws MessagingException Si l'envoi de l'email échoue
     */
    @Transactional
    public String createAndSendMfaCode(Integer userId, String userEmail, String userName) throws MessagingException {
        logger.info("Création d'un code MFA pour l'utilisateur ID: {}", userId);

        // Supprime les anciens codes non utilisés de cet utilisateur
        mfaCodeRepository.deleteUnusedCodesByUserId(userId);

        // Génère un nouveau code et un token de session
        String code = generateMfaCode();
        String sessionToken = generateSessionToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(mfaCodeExpirationMinutes);

        // Sauvegarde le code en base de données
        MfaCodeJpaEntity mfaCode = new MfaCodeJpaEntity(userId, code, expiresAt, sessionToken);
        mfaCodeRepository.save(mfaCode);

        logger.info("Code MFA créé pour l'utilisateur ID: {}, expire à: {}", userId, expiresAt);

        // Envoie le code par email
        try {
            emailService.sendMfaCode(userEmail, code, userName);
            logger.info("Code MFA envoyé avec succès à: {}", userEmail);
        } catch (MessagingException e) {
            logger.error("Échec de l'envoi du code MFA à {}: {}", userEmail, e.getMessage());
            // On supprime le code si l'envoi échoue
            mfaCodeRepository.delete(mfaCode);
            throw e;
        }

        return sessionToken;
    }

    /**
     * Valide un code MFA
     * 
     * @param sessionToken Token de session
     * @param code Code MFA saisi par l'utilisateur
     * @return ID de l'utilisateur si le code est valide, null sinon
     */
    @Transactional
    public Integer validateMfaCode(String sessionToken, String code) {
        logger.info("Validation du code MFA pour session: {}", sessionToken);

        // Recherche le code MFA valide
        var mfaCodeOpt = mfaCodeRepository.findValidCode(sessionToken, code, LocalDateTime.now());

        if (mfaCodeOpt.isEmpty()) {
            logger.warn("Code MFA invalide ou expiré pour session: {}", sessionToken);
            return null;
        }

        MfaCodeJpaEntity mfaCode = mfaCodeOpt.get();

        // Marque le code comme utilisé
        mfaCode.setUsed(true);
        mfaCodeRepository.save(mfaCode);

        logger.info("Code MFA validé avec succès pour utilisateur ID: {}", mfaCode.getUserId());
        return mfaCode.getUserId();
    }

    /**
     * Vérifie si un code MFA existe pour une session
     * 
     * @param sessionToken Token de session
     * @return true si un code existe (même expiré), false sinon
     */
    public boolean hasMfaCode(String sessionToken) {
        return mfaCodeRepository.findBySessionToken(sessionToken).isPresent();
    }

    /**
     * Récupère les informations d'un code MFA par session token
     * 
     * @param sessionToken Token de session
     * @return Entité MfaCode si trouvée, null sinon
     */
    public MfaCodeJpaEntity getMfaCodeBySessionToken(String sessionToken) {
        return mfaCodeRepository.findBySessionToken(sessionToken).orElse(null);
    }

    /**
     * Nettoie les codes MFA expirés et utilisés (tâche planifiée)
     * S'exécute toutes les heures
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredCodes() {
        logger.info("Nettoyage des codes MFA expirés et utilisés");
        try {
            mfaCodeRepository.deleteExpiredAndUsedCodes(LocalDateTime.now());
            logger.info("Nettoyage des codes MFA terminé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des codes MFA: {}", e.getMessage());
        }
    }
}
