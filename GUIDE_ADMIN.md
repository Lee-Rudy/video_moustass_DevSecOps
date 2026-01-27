# Guide d'utilisation - Interface d'administration

## Vue d'ensemble

Ce système permet à l'administrateur de gérer les utilisateurs de l'application via une interface web intuitive et moderne.

## Fonctionnalités

### 1. Connexion Administrateur

- L'admin se connecte avec son email et mot de passe sur la page de login
- Une fois connecté, il est automatiquement redirigé vers le **Dashboard Administrateur**
- Les utilisateurs normaux sont redirigés vers leur dashboard standard

### 2. Dashboard Administrateur (`/admin`)

Le dashboard admin affiche :
- **Actions rapides** : boutons pour créer des utilisateurs, consulter les logs, actualiser la liste
- **Liste complète des utilisateurs** avec :
  - ID, Nom, Email
  - Rôle (Administrateur ou Utilisateur)
  - Clé publique (tronquée)
  - Bouton de suppression

### 3. Création d'utilisateur (`/inscription`)

Le formulaire de création permet de :
- Renseigner le **nom** de l'utilisateur
- Renseigner l'**email** (validé automatiquement)
- Définir un **mot de passe** (min. 8 caractères avec au moins 1 majuscule)
- Confirmer le mot de passe
- Cocher optionnellement la case "Administrateur" (par défaut : false)

**Valeurs par défaut** :
- `isAdmin` : false
- `publicKey` : null (généré automatiquement)
- `vaultKey` : null (généré automatiquement)

### 4. Suppression d'utilisateur

- Bouton de suppression disponible pour chaque utilisateur
- Confirmation obligatoire avant suppression
- Les administrateurs ne peuvent pas être supprimés (protection)

### 5. Consultation des logs (`/logs`)

- Affichage de tous les logs d'audit du système
- Accessible à tous les utilisateurs connectés
- Les admins y ont accès depuis leur dashboard

## APIs Backend

### Endpoints disponibles

#### Créer un utilisateur
```
POST /api/inscription/create
Body: { name, mail, psw, isAdmin }
Response: UserResponse (avec id, name, mail, isAdmin, publicKey, vaultKey)
```

#### Lister tous les utilisateurs
```
GET /api/inscription/users
Headers: Authorization Bearer token
Response: List<UserResponse>
```

#### Supprimer un utilisateur
```
DELETE /api/inscription/users/{id}
Headers: Authorization Bearer token
Response: 204 No Content
```

#### Login
```
POST /api/login
Body: { mail, password }
Response: { token, userId, name, isAdmin }
```

## Architecture

### Backend (Java/Spring Boot)
- **InscriptionController** : Gestion des endpoints utilisateurs
- **InscriptionService** : Logique métier (création, liste, suppression)
- **InscriptionRepository** : Accès aux données
- **LoginService** : Authentification avec ajout du champ isAdmin

### Frontend (React)
- **Dashboard.jsx** : Interface admin avec liste et actions
- **Inscription.jsx** : Formulaire de création d'utilisateur
- **Login.jsx** : Connexion avec redirection conditionnelle
- **AuthContext** : Gestion de l'état d'authentification (inclut isAdmin)
- **Navbar** : Navigation adaptée au rôle (affiche les liens admin uniquement aux admins)

## Sécurité

- Les mots de passe sont hashés avec BCrypt
- Les clés publiques/privées sont générées automatiquement via Vault
- La clé privée reste dans Vault, seule la clé publique est stockée en DB
- Validation des emails avec regex
- Validation des mots de passe (longueur min, majuscule requise)

## Workflow de création d'utilisateur

1. Admin se connecte → redirigé vers `/admin`
2. Clic sur "Créer un utilisateur" → redirection vers `/inscription`
3. Remplissage du formulaire (nom, email, mot de passe)
4. Soumission du formulaire
5. Backend :
   - Hash du mot de passe
   - Génération d'une clé Vault unique (`user-signing-{UUID}`)
   - Création de la clé dans Vault
   - Export de la clé publique
   - Sauvegarde en base de données
6. Message de succès affiché
7. Admin peut créer un autre utilisateur ou retourner au dashboard

## Design

- Interface moderne et épurée
- Palette de couleurs cohérente (bleu indigo pour les actions principales)
- Formulaires avec validation en temps réel
- Messages d'erreur et de succès clairs
- Responsive design (fonctionne sur mobile et desktop)
- Tableaux avec effets de survol
- Badges colorés pour différencier les rôles

## Tests

Pour tester le système :

1. **Créer un admin** (via CommandLineRunner dans AuthApplication.java si nécessaire)
2. **Se connecter en tant qu'admin**
3. **Créer un utilisateur normal** via le formulaire
4. **Vérifier la liste des utilisateurs** dans le dashboard
5. **Supprimer un utilisateur** (confirmer la suppression)
6. **Consulter les logs** pour voir les actions tracées

## Notes techniques

- Les utilisateurs sont stockés dans la table `users`
- Les logs d'audit sont enregistrés automatiquement pour chaque action
- La navigation est protégée : les non-admins ne peuvent pas accéder aux pages admin
- Le token JWT est stocké dans localStorage et envoyé avec chaque requête
