# 🐳 Guide Docker - Application Vidéo Moustass DevSecOps

## 📋 Table des matières

1. [Prérequis](#-prérequis)
2. [Architecture Docker](#-architecture-docker)
3. [Configuration initiale](#-configuration-initiale)
4. [Build des images](#-build-des-images)
5. [Lancement de l'application](#-lancement-de-lapplication)
6. [Publication sur Docker Hub](#-publication-sur-docker-hub)
7. [Tests et vérifications](#-tests-et-vérifications)
8. [Gestion des données persistantes](#-gestion-des-données-persistantes)
9. [Dépannage](#-dépannage)
10. [Commandes utiles](#-commandes-utiles)

---

## 🔧 Prérequis

### Logiciels requis

- **Docker Desktop** (Windows/Mac) ou **Docker Engine** (Linux) : version 20.10+
- **Docker Compose** : version 2.0+
- **Git** : pour cloner le projet
- **Compte Docker Hub** : pour publier les images

### Vérification de l'installation

```bash
# Vérifier la version de Docker
docker --version

# Vérifier la version de Docker Compose
docker compose version

# Vérifier que Docker fonctionne
docker ps
```

---

## 🏗️ Architecture Docker

L'application est composée de **4 services** :

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React/Vite)                    │
│                      Port: 3000 -> 80                        │
│                      Nginx + SPA                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Backend (Spring Boot + OAuth2 + MFA)            │
│                      Port: 8082                              │
│              JWT + OAuth2 Google + Email MFA                 │
└──────────────┬────────────────────┬─────────────────────────┘
               │                    │
       ┌───────▼──────┐     ┌──────▼──────┐
       │    MySQL     │     │    Vault    │
       │  Port: 3307  │     │  Port: 8200 │
       │  (données)   │     │  (secrets)  │
       └──────────────┘     └─────────────┘
```

### Services

1. **MySQL** : Base de données avec persistance
2. **Vault** : Gestion des secrets et clés de chiffrement (Transit)
3. **Backend** : API Spring Boot avec OAuth2, MFA, JWT
4. **Frontend** : Interface React servie par Nginx

### Volumes persistants

- `moustass_mysql_data` : Données de la base de données
- `moustass_vault_data` : Secrets et clés Vault
- `moustass_vault_logs` : Logs Vault
- `moustass_video_storage` : Vidéos chiffrées

---

## ⚙️ Configuration initiale

### 1. Créer le fichier d'environnement

Copier le fichier d'exemple et le personnaliser :

```bash
# Windows PowerShell
Copy-Item .env.docker .env

# Linux/Mac
cp .env.docker .env
```

### 2. Éditer le fichier `.env`

Ouvrir le fichier `.env` et configurer vos valeurs :

```env
# Configuration MySQL
MYSQL_ROOT_PASSWORD=votre_mot_de_passe_root_securise
MYSQL_DATABASE=moustass_video
MYSQL_USER=moustass_user
MYSQL_PASSWORD=votre_mot_de_passe_utilisateur_securise

# Configuration Vault (Token DEV - changer en production)
VAULT_TOKEN=votre_token_vault_dev

# Configuration JWT (minimum 32 caractères)
JWT_SECRET=votre_secret_jwt_avec_au_moins_32_caracteres!

# Configuration Email SMTP pour MFA
SMTP_USERNAME=votre_email@gmail.com
SMTP_PASSWORD=votre_mot_de_passe_application_gmail

# Configuration OAuth2 Google
GOOGLE_CLIENT_ID=votre_client_id_google
GOOGLE_CLIENT_SECRET=votre_client_secret_google
OAUTH2_REDIRECT_URI=http://localhost:8082/login/oauth2/code/google

# Docker Hub
DOCKER_USERNAME=votre_username_dockerhub
VERSION=1.0.0
```

### 3. Configuration OAuth2 Google

⚠️ **Important** : Pour OAuth2, vous devez configurer les URIs de redirection dans Google Cloud Console :

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer/Sélectionner votre projet
3. Activer l'API "Google+ API"
4. Aller dans "Identifiants" > "ID client OAuth 2.0"
5. Ajouter les URIs autorisées :
   ```
   http://localhost:8082/login/oauth2/code/google
   http://localhost:3000
   ```

---

## 🔨 Build des images

### Build de toutes les images

```bash
# Build avec Docker Compose (recommandé)
docker compose build

# Build avec cache
docker compose build --no-cache

# Build parallèle (plus rapide)
docker compose build --parallel
```

### Build d'une image spécifique

```bash
# Backend uniquement
docker compose build backend

# Frontend uniquement
docker compose build frontend
```

### Build manuel (optionnel)

```bash
# Backend
docker build -t moustass/video-moustass-backend:latest -f Dockerfile.backend .

# Frontend
docker build -t moustass/video-moustass-frontend:latest -f Dockerfile.frontend .
```

---

## 🚀 Lancement de l'application

### 1. Arrêter les services locaux

⚠️ **Crucial** : Arrêter MySQL, Vault et l'application locale avant de lancer Docker :

```bash
# Arrêter MySQL local (Windows)
net stop MySQL80

# Arrêter les processus Spring Boot et React
# Ctrl+C dans les terminaux ou via le gestionnaire de tâches
```

### 2. Démarrer tous les services

```bash
# Démarrer en mode détaché (background)
docker compose up -d

# Démarrer en mode interactif (voir les logs)
docker compose up

# Démarrer avec rebuild automatique
docker compose up --build -d
```

### 3. Vérifier que tout fonctionne

```bash
# Vérifier l'état des conteneurs
docker compose ps

# Voir les logs
docker compose logs -f

# Logs d'un service spécifique
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f vault
```

### 4. Accéder à l'application

- **Frontend** : http://localhost:3000
- **Backend API** : http://localhost:8082/api
- **Vault UI** : http://localhost:8200/ui
- **MySQL** : localhost:3307 (port externe)

---

## 📤 Publication sur Docker Hub

### 1. Se connecter à Docker Hub

```bash
# Connexion interactive
docker login

# Connexion avec identifiants
docker login -u votre_username -p votre_password
```

### 2. Tagger les images

```bash
# Tag avec version spécifique
docker tag moustass/video-moustass-backend:latest votre_username/video-moustass-backend:1.0.0
docker tag moustass/video-moustass-frontend:latest votre_username/video-moustass-frontend:1.0.0

# Tag latest
docker tag moustass/video-moustass-backend:latest votre_username/video-moustass-backend:latest
docker tag moustass/video-moustass-frontend:latest votre_username/video-moustass-frontend:latest
```

### 3. Publier les images

```bash
# Push du backend
docker push votre_username/video-moustass-backend:1.0.0
docker push votre_username/video-moustass-backend:latest

# Push du frontend
docker push votre_username/video-moustass-frontend:1.0.0
docker push votre_username/video-moustass-frontend:latest
```

### 4. Script automatisé complet

Créer un fichier `publish-dockerhub.sh` (Linux/Mac) :

```bash
#!/bin/bash

# Variables
DOCKER_USERNAME="votre_username"
VERSION="1.0.0"

# Build
echo "🔨 Build des images..."
docker compose build

# Tag
echo "🏷️  Tag des images..."
docker tag moustass/video-moustass-backend:latest $DOCKER_USERNAME/video-moustass-backend:$VERSION
docker tag moustass/video-moustass-backend:latest $DOCKER_USERNAME/video-moustass-backend:latest
docker tag moustass/video-moustass-frontend:latest $DOCKER_USERNAME/video-moustass-frontend:$VERSION
docker tag moustass/video-moustass-frontend:latest $DOCKER_USERNAME/video-moustass-frontend:latest

# Push
echo "📤 Publication sur Docker Hub..."
docker push $DOCKER_USERNAME/video-moustass-backend:$VERSION
docker push $DOCKER_USERNAME/video-moustass-backend:latest
docker push $DOCKER_USERNAME/video-moustass-frontend:$VERSION
docker push $DOCKER_USERNAME/video-moustass-frontend:latest

echo "✅ Publication terminée !"
```

Pour Windows PowerShell, créer `publish-dockerhub.ps1` :

```powershell
# Variables
$DOCKER_USERNAME = "votre_username"
$VERSION = "1.0.0"

# Build
Write-Host "🔨 Build des images..." -ForegroundColor Green
docker compose build

# Tag
Write-Host "🏷️  Tag des images..." -ForegroundColor Green
docker tag moustass/video-moustass-backend:latest ${DOCKER_USERNAME}/video-moustass-backend:${VERSION}
docker tag moustass/video-moustass-backend:latest ${DOCKER_USERNAME}/video-moustass-backend:latest
docker tag moustass/video-moustass-frontend:latest ${DOCKER_USERNAME}/video-moustass-frontend:${VERSION}
docker tag moustass/video-moustass-frontend:latest ${DOCKER_USERNAME}/video-moustass-frontend:latest

# Push
Write-Host "📤 Publication sur Docker Hub..." -ForegroundColor Green
docker push ${DOCKER_USERNAME}/video-moustass-backend:${VERSION}
docker push ${DOCKER_USERNAME}/video-moustass-backend:latest
docker push ${DOCKER_USERNAME}/video-moustass-frontend:${VERSION}
docker push ${DOCKER_USERNAME}/video-moustass-frontend:latest

Write-Host "✅ Publication terminée !" -ForegroundColor Green
```

Exécuter :

```bash
# Linux/Mac
chmod +x publish-dockerhub.sh
./publish-dockerhub.sh

# Windows PowerShell
.\publish-dockerhub.ps1
```

---

## ✅ Tests et vérifications

### 1. Health checks

```bash
# Vérifier les health checks des conteneurs
docker compose ps

# Health check manuel
curl http://localhost:8082/actuator/health  # Backend
curl http://localhost:3000                   # Frontend
curl http://localhost:8200/v1/sys/health    # Vault
```

### 2. Tests fonctionnels

#### a) Test de la base de données

```bash
# Se connecter à MySQL
docker exec -it moustass-mysql mysql -u root -p

# Entrer le mot de passe configuré dans .env
# Puis vérifier les tables :
USE moustass_video;
SHOW TABLES;
SELECT * FROM users;
exit;
```

#### b) Test de Vault

```bash
# Se connecter au conteneur Vault
docker exec -it moustass-vault sh

# Vérifier la clé Transit
export VAULT_TOKEN=dev-only-token-change-in-prod
vault read transit/keys/video-dek
exit
```

#### c) Test du Backend (OAuth2 + MFA)

```bash
# Test de l'endpoint de santé
curl http://localhost:8082/api/users/health

# Test de la liste des utilisateurs (nécessite un token)
curl http://localhost:8082/api/users

# Test OAuth2 Google
# Ouvrir dans le navigateur : http://localhost:3000
# Cliquer sur "Se connecter avec Google"
```

#### d) Test du Frontend

```bash
# Ouvrir dans le navigateur
http://localhost:3000

# Tester :
# 1. Page d'accueil
# 2. Inscription classique
# 3. Connexion avec Google (OAuth2)
# 4. Réception du code MFA par email
# 5. Accès au dashboard
```

### 3. Tests de persistance

```bash
# 1. Créer un utilisateur via l'interface
# 2. Arrêter les conteneurs
docker compose down

# 3. Redémarrer
docker compose up -d

# 4. Vérifier que l'utilisateur existe toujours
docker exec -it moustass-mysql mysql -u root -p
USE moustass_video;
SELECT * FROM users;
exit;
```

---

## 💾 Gestion des données persistantes

### Lister les volumes

```bash
# Lister tous les volumes Docker
docker volume ls

# Inspecter un volume spécifique
docker volume inspect moustass_mysql_data
docker volume inspect moustass_vault_data
docker volume inspect moustass_video_storage
```

### Backup des données

#### Backup MySQL

```bash
# Créer un backup
docker exec moustass-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} moustass_video > backup_$(date +%Y%m%d).sql

# Restaurer un backup
docker exec -i moustass-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} moustass_video < backup_20260130.sql
```

#### Backup Vault

```bash
# Créer un backup des données Vault
docker run --rm -v moustass_vault_data:/data -v $(pwd):/backup alpine tar czf /backup/vault_backup_$(date +%Y%m%d).tar.gz -C /data .

# Restaurer un backup Vault
docker run --rm -v moustass_vault_data:/data -v $(pwd):/backup alpine tar xzf /backup/vault_backup_20260130.tar.gz -C /data
```

### Supprimer les données

⚠️ **Attention** : Cela supprime TOUTES les données !

```bash
# Arrêter et supprimer les conteneurs + volumes
docker compose down -v

# Supprimer un volume spécifique
docker volume rm moustass_mysql_data
docker volume rm moustass_vault_data
docker volume rm moustass_video_storage
```

---

## 🔍 Dépannage

### Problème 1 : Conteneur ne démarre pas

```bash
# Voir les logs détaillés
docker compose logs backend
docker compose logs mysql
docker compose logs vault

# Redémarrer un service spécifique
docker compose restart backend
```

### Problème 2 : Erreur de connexion MySQL

```bash
# Vérifier que MySQL est bien démarré
docker compose ps mysql

# Vérifier les logs MySQL
docker compose logs mysql

# Se connecter manuellement
docker exec -it moustass-mysql mysql -u root -p
```

### Problème 3 : Vault non initialisé

```bash
# Vérifier les logs d'initialisation
docker compose logs vault-init

# Réinitialiser Vault
docker compose restart vault
docker compose restart vault-init
```

### Problème 4 : OAuth2 ne fonctionne pas

1. Vérifier les URIs de redirection dans Google Cloud Console
2. Vérifier les variables d'environnement dans `.env`
3. Vérifier les logs du backend :
   ```bash
   docker compose logs backend | grep -i oauth
   ```

### Problème 5 : MFA - Email non reçu

```bash
# Vérifier la configuration SMTP
docker compose logs backend | grep -i mail
docker compose logs backend | grep -i mfa

# Vérifier que le compte Gmail est configuré pour les applications moins sécurisées
```

### Problème 6 : Port déjà utilisé

```bash
# Trouver le processus utilisant le port
# Windows
netstat -ano | findstr :3000
netstat -ano | findstr :8082
netstat -ano | findstr :3307

# Linux/Mac
lsof -i :3000
lsof -i :8082
lsof -i :3307

# Tuer le processus (remplacer PID)
# Windows
taskkill /PID <PID> /F

# Linux/Mac
kill -9 <PID>
```

---

## 📚 Commandes utiles

### Gestion des conteneurs

```bash
# Démarrer
docker compose up -d

# Arrêter
docker compose down

# Redémarrer
docker compose restart

# Arrêter et supprimer les volumes
docker compose down -v

# Reconstruire et redémarrer
docker compose up --build -d

# Voir les conteneurs en cours
docker compose ps

# Voir tous les conteneurs (même arrêtés)
docker compose ps -a
```

### Logs

```bash
# Tous les logs
docker compose logs

# Logs en temps réel
docker compose logs -f

# Logs d'un service
docker compose logs backend
docker compose logs -f backend

# Dernières 100 lignes
docker compose logs --tail=100

# Logs depuis une date
docker compose logs --since 2026-01-30T10:00:00
```

### Accès aux conteneurs

```bash
# Shell dans le conteneur backend
docker exec -it moustass-backend sh

# Shell dans MySQL
docker exec -it moustass-mysql mysql -u root -p

# Shell dans Vault
docker exec -it moustass-vault sh

# Shell dans frontend (Nginx)
docker exec -it moustass-frontend sh
```

### Monitoring

```bash
# Statistiques en temps réel
docker stats

# Utilisation disque
docker system df

# Inspecter un conteneur
docker inspect moustass-backend

# Voir les processus d'un conteneur
docker top moustass-backend
```

### Nettoyage

```bash
# Nettoyer les images non utilisées
docker image prune

# Nettoyer les conteneurs arrêtés
docker container prune

# Nettoyer les volumes non utilisés
docker volume prune

# Nettoyage complet (⚠️ ATTENTION)
docker system prune -a --volumes
```

---

## 🎯 Résumé des commandes essentielles

### Cycle de vie complet

```bash
# 1. Configuration initiale (une seule fois)
cp .env.docker .env
# Éditer le .env avec vos valeurs

# 2. Build des images
docker compose build

# 3. Démarrer l'application
docker compose up -d

# 4. Vérifier que tout fonctionne
docker compose ps
docker compose logs -f

# 5. Tester l'application
# Frontend: http://localhost:3000
# Backend: http://localhost:8082

# 6. Publication sur Docker Hub
docker login
docker compose build
docker tag moustass/video-moustass-backend:latest votre_username/video-moustass-backend:1.0.0
docker tag moustass/video-moustass-frontend:latest votre_username/video-moustass-frontend:1.0.0
docker push votre_username/video-moustass-backend:1.0.0
docker push votre_username/video-moustass-frontend:1.0.0

# 7. Arrêter l'application
docker compose down

# 8. Redémarrer (les données persistent)
docker compose up -d
```

---

## 🔐 Sécurité en Production

⚠️ **Recommandations importantes** :

1. **Ne jamais committer le fichier `.env`** avec des vraies valeurs
2. **Changer tous les mots de passe par défaut**
3. **Utiliser des secrets Docker** pour les valeurs sensibles
4. **Activer HTTPS** avec un reverse proxy (Traefik, Nginx)
5. **Utiliser Vault en mode production** (pas en mode dev)
6. **Limiter les ressources** des conteneurs (CPU, RAM)
7. **Configurer des health checks** robustes
8. **Mettre en place des backups automatiques**
9. **Utiliser des images officielles** et les mettre à jour régulièrement
10. **Scanner les images** pour les vulnérabilités (Trivy, Snyk)

---

## 📞 Support

Pour toute question ou problème :

1. Vérifier les logs : `docker compose logs -f`
2. Consulter la section [Dépannage](#-dépannage)
3. Vérifier la documentation officielle :
   - [Docker Compose](https://docs.docker.com/compose/)
   - [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
   - [HashiCorp Vault](https://www.vaultproject.io/docs)
   - [MySQL Docker](https://hub.docker.com/_/mysql)

---

## ✅ Checklist finale

Avant de mettre en production :

- [ ] Tous les services démarrent correctement
- [ ] Les health checks sont verts
- [ ] Les données persistent après un redémarrage
- [ ] OAuth2 Google fonctionne
- [ ] MFA par email fonctionne
- [ ] Vault chiffre/déchiffre correctement
- [ ] Les backups sont configurés
- [ ] Les mots de passe sont sécurisés
- [ ] Les images sont publiées sur Docker Hub
- [ ] La documentation est à jour

---

**Bonne dockerisation ! 🐳🚀**
