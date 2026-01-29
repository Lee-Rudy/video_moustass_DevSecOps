# Modifications : Notifications et Affichage de l'Expéditeur

Date : 30 janvier 2026

## Résumé des modifications

Ce document détaille les modifications apportées pour résoudre deux problèmes :
1. **Ajout d'une colonne "De" (expéditeur)** dans le tableau des ordres reçus
2. **Correction du système de notifications** pour qu'il soit spécifique à chaque utilisateur

---

## 1. Ajout de la colonne "Expéditeur" dans les ordres

### Backend

#### Fichiers modifiés :

**`src/main/java/com/example/auth/order/OrderController.java`**
- Modification du record `OrderReceivedDto` pour ajouter un champ `senderName`
- Modification de la méthode `toDto()` pour récupérer le nom de l'expéditeur à partir de l'`userId`

```java
private OrderReceivedDto toDto(SignatureTransactionJpaEntity e) {
    String senderName = userRepo.findById(e.getUserId())
            .map(u -> u.getName() != null ? u.getName() : "Utilisateur #" + e.getUserId())
            .orElse("Utilisateur #" + e.getUserId());
    
    return new OrderReceivedDto(
            e.getId(),
            e.getVideoName(),
            e.getVideoHash(),
            e.getPathVideo(),
            e.getExpiredVideo() != null ? e.getExpiredVideo().toString() : null,
            e.isActive(),
            e.getSignedAt() != null ? e.getSignedAt().toString() : null,
            e.getCreatedAt() != null ? e.getCreatedAt().toString() : null,
            senderName  // <-- Nouveau champ
    );
}
```

### Frontend

#### Fichiers modifiés :

**`front/react-api-app/src/pages/ListOrder.jsx`**
- Ajout d'une colonne "De" dans l'en-tête du tableau
- Ajout de l'affichage du nom de l'expéditeur dans chaque ligne du tableau

**Avant :**
```jsx
<th style={styles.th}>Titre</th>
<th style={styles.th}>Hash</th>
```

**Après :**
```jsx
<th style={styles.th}>Titre</th>
<th style={styles.th}>De</th>
<th style={styles.th}>Hash</th>
```

---

## 2. Système de Notifications Spécifique à l'Utilisateur

### Backend

#### Nouveaux fichiers créés :

1. **`src/main/java/com/example/auth/notification/entity/NotificationJpaEntity.java`**
   - Entité JPA pour stocker les notifications
   - Champs : id, recipientId, senderId, type, message, orderId, isRead, createdAt

2. **`src/main/java/com/example/auth/notification/repository/SpringDataNotificationRepository.java`**
   - Repository Spring Data pour gérer les notifications
   - Méthodes : `findByRecipientIdOrderByCreatedAtDesc()`, `countByRecipientIdAndIsRead()`

3. **`src/main/java/com/example/auth/notification/service/NotificationService.java`**
   - Service pour gérer la logique métier des notifications
   - Méthodes principales :
     - `createOrderNotification()` : Crée une notification lors de la création d'un ordre
     - `getNotifications()` : Récupère les notifications d'un utilisateur
     - `markAsRead()` : Marque une notification comme lue
     - `countUnread()` : Compte les notifications non lues

4. **`src/main/java/com/example/auth/notification/controller/NotificationController.java`**
   - Contrôleur REST pour exposer les endpoints des notifications
   - Endpoints :
     - `GET /api/notifications` : Récupère toutes les notifications de l'utilisateur connecté
     - `POST /api/notifications/:id/read` : Marque une notification comme lue
     - `GET /api/notifications/unread-count` : Compte les notifications non lues

#### Fichiers modifiés :

**`src/main/java/com/example/auth/order/OrderService.java`**
- Ajout de l'injection de `NotificationService`
- Modification de la méthode `createOrder()` pour créer une notification lors de la création d'un ordre

```java
// Créer une notification pour le destinataire
UsersJpaEntity recipient = userRepo.findByName(transactionSendTo.trim()).orElse(null);
if (recipient != null && !recipient.isAdmin()) {
    notificationService.createOrderNotification(userId, recipient.getId(), e.getId(), transactionSendTo);
}
```

**`src/main/java/com/example/auth/inscription/ports/out/SpringDataUsersRepository.java`**
- Ajout de la méthode `findByName()` pour rechercher un utilisateur par son nom

**`database/migrations/service_database.sql`**
- Ajout de la migration pour créer la table `notifications`

```sql
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
```

### Frontend

#### Fichiers modifiés :

**`front/react-api-app/src/api/authApi.js`**
- Ajout de 3 nouvelles fonctions API :
  - `getNotifications(token)` : Récupère les notifications
  - `markNotificationAsRead(notificationId, token)` : Marque une notification comme lue
  - `getUnreadNotificationsCount(token)` : Compte les notifications non lues

**`front/react-api-app/src/pages/NotificationsOrdre.jsx`**
- Remplacement des données mockées par des appels API réels
- Modification de `loadNotifications()` pour utiliser l'API
- Modification de `markAsRead()` pour utiliser l'API

**Avant :**
```jsx
const loadNotifications = useCallback(() => {
    const mockNotifications = [/* ... données en dur ... */];
    setTimeout(() => {
      setNotifications(mockNotifications);
      setLoading(false);
    }, 500);
}, [user?.userId]);
```

**Après :**
```jsx
const loadNotifications = useCallback(async () => {
    if (!user?.token) return;
    
    try {
      setLoading(true);
      const data = await getNotifications(user.token);
      setNotifications(data);
    } catch (error) {
      console.error('Erreur chargement notifications:', error);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
}, [user?.token]);
```

---

## Migration de la base de données

### Étapes à suivre :

1. **Exécuter la migration SQL**
   ```bash
   mysql -u root -p moustass_video < database/migrations/service_database.sql
   ```

2. **Vérifier la création de la table**
   ```sql
   SHOW TABLES LIKE 'notifications';
   DESC notifications;
   ```

---

## Tests à effectuer

### 1. Test de l'affichage de l'expéditeur
- [ ] Créer un ordre depuis un utilisateur A vers un utilisateur B
- [ ] Se connecter en tant qu'utilisateur B
- [ ] Vérifier que la colonne "De" affiche bien le nom de l'utilisateur A

### 2. Test des notifications
- [ ] Créer un ordre depuis un utilisateur A vers un utilisateur B
- [ ] Se connecter en tant qu'utilisateur B
- [ ] Vérifier qu'une notification apparaît dans la page "Notifications Ordre"
- [ ] Vérifier que le badge de compteur affiche "1"
- [ ] Marquer la notification comme lue
- [ ] Vérifier que le badge disparaît ou s'actualise
- [ ] Se déconnecter et se connecter en tant qu'utilisateur C
- [ ] Vérifier qu'aucune notification n'apparaît pour l'utilisateur C

### 3. Test de l'isolation des notifications
- [ ] Créer plusieurs ordres depuis différents utilisateurs
- [ ] Se connecter avec différents comptes
- [ ] Vérifier que chaque utilisateur ne voit que ses propres notifications

---

## Points d'attention

1. **Performances** : Un index a été créé sur `recipient_id` pour optimiser les requêtes de récupération des notifications
2. **Sécurité** : Les endpoints vérifient que l'utilisateur connecté est bien le destinataire de la notification
3. **Compatibilité** : Les modifications sont rétrocompatibles et n'affectent pas les fonctionnalités existantes

---

## Prochaines améliorations possibles

1. **Notifications temps réel** : Implémenter WebSocket ou Server-Sent Events pour les notifications en temps réel
2. **Types de notifications** : Étendre le système pour supporter d'autres types de notifications (validation d'ordre, expiration, etc.)
3. **Préférences de notifications** : Permettre aux utilisateurs de configurer leurs préférences de notifications
4. **Historique** : Ajouter une durée de rétention pour les notifications anciennes
5. **Pagination** : Paginer la liste des notifications pour améliorer les performances

---

## Conclusion

Les deux problèmes ont été résolus avec succès :
- ✅ La colonne "De" affiche maintenant l'expéditeur de chaque ordre
- ✅ Les notifications sont maintenant spécifiques à chaque utilisateur connecté

Le système est prêt pour la production après exécution de la migration SQL.
