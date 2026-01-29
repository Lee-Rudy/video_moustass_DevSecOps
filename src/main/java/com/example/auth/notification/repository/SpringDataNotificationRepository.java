package com.example.auth.notification.repository;

import com.example.auth.notification.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Integer> {
    
    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);
    
    long countByRecipientIdAndIsRead(Integer recipientId, boolean isRead);
}
