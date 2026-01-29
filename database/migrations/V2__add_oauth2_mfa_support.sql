-- Migration V2: Ajout du support OAuth2 et MFA
-- Date: 2026-01-29
-- Description: Ajoute les colonnes OAuth2 à la table users et crée la table mfa_codes

-- Ajout des colonnes OAuth2 à la table users
ALTER TABLE users
ADD COLUMN oauth_provider VARCHAR(50) NULL,
ADD COLUMN oauth_provider_id VARCHAR(255) NULL,
ADD INDEX idx_oauth_provider (oauth_provider, oauth_provider_id);

-- Création de la table mfa_codes pour stocker les codes MFA temporaires
CREATE TABLE IF NOT EXISTS mfa_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used TINYINT(1) NOT NULL DEFAULT 0,
    session_token TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_mfa_user_id (user_id),
    INDEX idx_mfa_expires_at (expires_at)
);
