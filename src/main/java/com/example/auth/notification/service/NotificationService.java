package com.example.auth.notification.service;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.notification.entity.NotificationJpaEntity;
import com.example.auth.notification.repository.SpringDataNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final SpringDataNotificationRepository notificationRepo;
    private final SpringDataUsersRepository userRepo;

    public NotificationService(SpringDataNotificationRepository notificationRepo, SpringDataUsersRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    /**
     * Crée une notification pour un ordre reçu
     */
    public void createOrderNotification(Integer senderId, Integer recipientId, Integer orderId, String recipientName) {
        UsersJpaEntity sender = userRepo.findById(senderId).orElse(null);
        String senderName = sender != null && sender.getName() != null ? sender.getName() : "Utilisateur #" + senderId;
        
        NotificationJpaEntity notification = new NotificationJpaEntity();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType("ORDRE_RECU");
        notification.setMessage(senderName + " vous a envoyé un ordre");
        notification.setOrderId(orderId);
        notification.setRead(false);
        
        notificationRepo.save(notification);
    }

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<NotificationDto> getNotifications(Integer userId) {
        List<NotificationJpaEntity> notifications = notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Marque une notification comme lue
     */
    public void markAsRead(Integer notificationId, Integer userId) {
        NotificationJpaEntity notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable"));
        
        if (!notification.getRecipientId().equals(userId)) {
            throw new IllegalArgumentException("Cette notification ne vous appartient pas");
        }
        
        notification.setRead(true);
        notificationRepo.save(notification);
    }

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public long countUnread(Integer userId) {
        return notificationRepo.countByRecipientIdAndIsRead(userId, false);
    }

    private NotificationDto toDto(NotificationJpaEntity n) {
        UsersJpaEntity sender = userRepo.findById(n.getSenderId()).orElse(null);
        String senderName = sender != null && sender.getName() != null ? sender.getName() : "Utilisateur #" + n.getSenderId();
        
        return new NotificationDto(
                n.getId(),
                n.getType(),
                senderName,
                n.getMessage(),
                n.getOrderId(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : null,
                !n.isRead()
        );
    }

    public record NotificationDto(
            Integer id,
            String type,
            String expediteur,
            String message,
            Integer ordreId,
            String date,
            boolean isNew
    ) {}
}
