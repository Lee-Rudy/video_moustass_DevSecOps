package com.example.auth.inscription.ports.out;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;

public interface SpringDataUsersRepository extends JpaRepository<UsersJpaEntity, Integer> {

    boolean existsByMail(String mail);

    java.util.Optional<UsersJpaEntity> findByMail(String mail);
    
    /** Retourne tous les utilisateurs avec cet email (pour gérer les doublons) */
    java.util.List<UsersJpaEntity> findAllByMail(String mail);

    /** Utilisateurs non-admin pour le champ « Envoyé à » (Order). */
    java.util.List<UsersJpaEntity> findByIsAdminFalse();

    /** Recherche un utilisateur par OAuth provider et provider ID */
    java.util.Optional<UsersJpaEntity> findByOauthProviderAndOauthProviderId(String oauthProvider, String oauthProviderId);
    
    /** Recherche un utilisateur par son nom */
    java.util.Optional<UsersJpaEntity> findByName(String name);
}
