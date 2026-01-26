package com.example.auth.audit.repository;

import com.example.auth.audit.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogJpaEntity, Long> {
    
    /**
     * Récupère tous les logs triés par date décroissante (plus récents en premier).
     */
    List<AuditLogJpaEntity> findAllByOrderByCreatedAtDesc();
}
