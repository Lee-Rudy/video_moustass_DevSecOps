package com.example.auth.audit.service;

import com.example.auth.audit.entity.AuditLogJpaEntity;
import com.example.auth.audit.repository.AuditLogRepository;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SpringDataUsersRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, SpringDataUsersRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crée un log d'audit pour une action utilisateur.
     */
    public void logAction(Integer userId, String action, String entity, Integer entityId, String message, HttpServletRequest request) {
        UsersJpaEntity user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setActorUserId(userId);
        log.setActorName(user != null ? user.getName() : null);
        log.setActorMail(user != null ? user.getMail() : null);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setMessage(message);
        log.setIpAddress(extractIpAddress(request));
        log.setUserAgent(extractUserAgent(request));
        log.setMetadata(null);

        auditLogRepository.save(log);
    }

    /**
     * Crée un log d'audit avec métadonnées JSON.
     */
    public void logAction(Integer userId, String action, String entity, Integer entityId, String message, Map<String, Object> metadata, HttpServletRequest request) {
        UsersJpaEntity user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setActorUserId(userId);
        log.setActorName(user != null ? user.getName() : null);
        log.setActorMail(user != null ? user.getMail() : null);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setMessage(message);
        log.setIpAddress(extractIpAddress(request));
        log.setUserAgent(extractUserAgent(request));
        log.setMetadata(metadata != null ? mapToJson(metadata) : null);

        auditLogRepository.save(log);
    }

    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.length() > 255 ? ua.substring(0, 255) : ua;
    }

    private String mapToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
