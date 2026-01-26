package com.example.auth.audit.controller;

import com.example.auth.audit.entity.AuditLogJpaEntity;
import com.example.auth.audit.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * GET /api/logs : retourne tous les logs triés par date décroissante.
     * Accessible à tous les utilisateurs connectés.
     */
    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAllLogs() {
        List<AuditLogJpaEntity> logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        List<AuditLogDto> dtos = logs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private AuditLogDto toDto(AuditLogJpaEntity log) {
        return new AuditLogDto(
                log.getId(),
                log.getActorUserId(),
                log.getActorName(),
                log.getActorMail(),
                log.getAction(),
                log.getEntity(),
                log.getEntityId(),
                log.getMessage(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getMetadata(),
                log.getCreatedAt() != null ? log.getCreatedAt().toString() : null
        );
    }

    public record AuditLogDto(
            Long id,
            Integer actorUserId,
            String actorName,
            String actorMail,
            String action,
            String entity,
            Integer entityId,
            String message,
            String ipAddress,
            String userAgent,
            String metadata,
            String createdAt
    ) {}
}
