package com.example.auth.mfa.repository;

import com.example.auth.mfa.entity.MfaCodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository pour la gestion des codes MFA
 */
@Repository
public interface MfaCodeRepository extends JpaRepository<MfaCodeJpaEntity, Long> {

    /**
     * Trouve un code MFA valide par session token et code
     */
    @Query("SELECT m FROM MfaCodeJpaEntity m WHERE m.sessionToken = :sessionToken AND m.code = :code AND m.isUsed = false AND m.expiresAt > :now")
    Optional<MfaCodeJpaEntity> findValidCode(
        @Param("sessionToken") String sessionToken,
        @Param("code") String code,
        @Param("now") LocalDateTime now
    );

    /**
     * Trouve un code MFA par session token (peu importe s'il est utilisé ou expiré)
     */
    Optional<MfaCodeJpaEntity> findBySessionToken(String sessionToken);

    /**
     * Supprime tous les codes MFA expirés
     */
    @Modifying
    @Query("DELETE FROM MfaCodeJpaEntity m WHERE m.expiresAt < :now OR m.isUsed = true")
    void deleteExpiredAndUsedCodes(@Param("now") LocalDateTime now);

    /**
     * Supprime tous les codes MFA non utilisés d'un utilisateur
     */
    @Modifying
    @Query("DELETE FROM MfaCodeJpaEntity m WHERE m.userId = :userId AND m.isUsed = false")
    void deleteUnusedCodesByUserId(@Param("userId") Integer userId);
}
