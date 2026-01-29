-- database name : moustass_video

-- microservice authentification
CREATE TABLE users 
(
    id INT AUTO_INCREMENT PRIMARY KEY not null,
    name text,
    mail text NOT NULL,
    psw_hash text NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE, -- true = admin
    public_key text,
    vault_key text,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- data test

-- insert into (id, name, mail, psw_hash, is_admin) users values(1, 'admin', 'ewenbruce82@gmail.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', true);

-- insert into (id, name, mail, psw_hash, is_admin) users values(2, 'Rudy', 'brunerleerudy@gmail.com', '6cc058c5cd9578c5611b0fd9532317bdc45d1a8309945ef4d1c5d2e3a2268152', false);


--============================================================================

CREATE TABLE signature_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_send_to TEXT NOT NULL,
    video_name VARCHAR(500) NOT NULL,
    montant_transaction DECIMAL(10,2) NOT NULL,
    video_hash VARCHAR(500) NOT NULL,
    path_video TEXT NOT NULL,
    expired_video DATETIME NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    public_key TEXT NOT NULL,
    signature TEXT NOT NULL,
    signed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_orders_user_id (user_id),
    INDEX idx_sig_video_hash (video_hash)
);

-- microservice transactions 
-- create table signature_transactions 
-- (
--     id int auto_increment primary key not null,
--     user_id int not null,
--     transaction_send_to text not null,
--     video_name varchar(255) not null,
--     montant_transaction decimal (10,2) not null,
--     video_hash text not null,
--     path_video text not null,
--     expired_video datetime not null,
--     is_active boolean not null default true,
--     public_key text not null, -- public key de celui qui l'a signé
--     signature text not null,
--     signed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

--     index idx_orders_user_id (user_id), --indexation de recherche de user_id
--     INDEX idx_sig_video_hash (video_hash)
-- );

--===========================================================================
-- qui s'est connecté + quand ?
-- qu'est ce qu'il a fait + quand?
CREATE TABLE audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,

  -- QUI (sans FK, juste une copie “pratique”)
  actor_user_id INT NULL,
  actor_name VARCHAR(255) NULL,
  actor_mail VARCHAR(255) NULL,

  -- QUOI
  action VARCHAR(80) NOT NULL,          -- ex: USER_LOGIN, TX_CREATED, TX_SIGNED, TX_DISABLED
  entity VARCHAR(60) NOT NULL,          -- ex: users, signature_transactions
  entity_id INT NULL,                   -- id concerné (user.id ou signature_transactions.id)
  message VARCHAR(500) NULL,            -- résumé lisible

  -- CONTEXTE MINIMAL
  ip_address VARCHAR(45) NULL,          -- IPv4/IPv6
  user_agent VARCHAR(255) NULL,
  metadata TEXT NULL,                   -- JSON texte possible (facultatif)

  -- QUAND
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_logs_actor_user_id (actor_user_id),
  INDEX idx_logs_entity (entity, entity_id),
  INDEX idx_logs_created_at (created_at),
  INDEX idx_logs_action (action)
);



-- ====================================

-- suite de la database
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

-- ====================================
-- Migration V3: Ajout du système de notifications
-- Date: 2026-01-30
-- Description: Crée la table notifications pour gérer les notifications des utilisateurs

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id INT NOT NULL,
    sender_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    order_id INT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_notifications_recipient_id (recipient_id),
    INDEX idx_notifications_is_read (is_read)
);

