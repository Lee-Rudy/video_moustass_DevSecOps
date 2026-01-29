# 🚀 Aide-mémoire des Commandes Docker

Guide de référence rapide pour toutes les commandes Docker.

---

## 🎯 Commandes de base

### Démarrage et arrêt

```bash
# Démarrer tous les services
docker compose up -d

# Démarrer en mode interactif (voir les logs)
docker compose up

# Arrêter tous les services
docker compose down

# Arrêter et supprimer les volumes (⚠️ ATTENTION : supprime les données)
docker compose down -v

# Redémarrer tous les services
docker compose restart

# Redémarrer un service spécifique
docker compose restart backend
```

### Build

```bash
# Build de toutes les images
docker compose build

# Build sans cache
docker compose build --no-cache

# Build d'un service spécifique
docker compose build backend

# Build et démarrage
docker compose up --build -d
```

---

## 📊 Surveillance et monitoring

### État des services

```bash
# Voir l'état de tous les conteneurs
docker compose ps

# Voir tous les conteneurs (même arrêtés)
docker compose ps -a

# Voir les statistiques en temps réel
docker stats

# Voir les statistiques d'un conteneur
docker stats moustass-backend

# Inspecter un conteneur
docker inspect moustass-backend
```

### Logs

```bash
# Voir tous les logs
docker compose logs

# Logs en temps réel
docker compose logs -f

# Logs d'un service spécifique
docker compose logs backend
docker compose logs -f backend

# Dernières 100 lignes
docker compose logs --tail=100

# Logs depuis une date/heure
docker compose logs --since 2026-01-30T10:00:00

# Logs avec timestamps
docker compose logs -f -t
```

---

## 🔧 Accès aux conteneurs

### Shell

```bash
# Shell dans le backend
docker exec -it moustass-backend sh

# Shell dans le frontend
docker exec -it moustass-frontend sh

# Shell dans Vault
docker exec -it moustass-vault sh

# Shell dans MySQL
docker exec -it moustass-mysql bash
```

### MySQL

```bash
# Connexion MySQL
docker exec -it moustass-mysql mysql -u root -p

# Requête directe
docker exec -it moustass-mysql mysql -u root -p -e "SHOW DATABASES;"

# Voir les utilisateurs
docker exec -it moustass-mysql mysql -u root -p -e "SELECT * FROM moustass_video.users;"

# Voir les tables
docker exec -it moustass-mysql mysql -u root -p -e "USE moustass_video; SHOW TABLES;"
```

### Vault

```bash
# Vérifier le status de Vault
curl http://localhost:8200/v1/sys/health | jq

# Se connecter à Vault
docker exec -it moustass-vault sh

# Dans le shell Vault :
export VAULT_TOKEN=dev-only-token-change-in-prod
export VAULT_ADDR=http://localhost:8200

# Lire une clé Transit
vault read transit/keys/video-dek

# Chiffrer
vault write transit/encrypt/video-dek plaintext=$(echo "Test" | base64)

# Déchiffrer
vault write transit/decrypt/video-dek ciphertext="vault:v1:..."
```

---

## 💾 Gestion des volumes

### Lister et inspecter

```bash
# Lister tous les volumes
docker volume ls

# Lister les volumes de l'application
docker volume ls | grep moustass

# Inspecter un volume
docker volume inspect moustass_mysql_data
docker volume inspect moustass_vault_data
docker volume inspect moustass_video_storage
```

### Backup et restauration

```bash
# Backup MySQL
docker exec moustass-mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} moustass_video > backup_$(date +%Y%m%d).sql

# Windows PowerShell
$date = Get-Date -Format "yyyyMMdd"
docker exec moustass-mysql mysqldump -u root -p${env:MYSQL_ROOT_PASSWORD} moustass_video > backup_$date.sql

# Restaurer MySQL
docker exec -i moustass-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} moustass_video < backup_20260130.sql

# Backup Vault (Linux/Mac)
docker run --rm -v moustass_vault_data:/data -v $(pwd):/backup alpine tar czf /backup/vault_backup_$(date +%Y%m%d).tar.gz -C /data .

# Backup Vault (Windows PowerShell)
docker run --rm -v moustass_vault_data:/data -v ${PWD}:/backup alpine tar czf /backup/vault_backup.tar.gz -C /data .

# Restaurer Vault
docker run --rm -v moustass_vault_data:/data -v $(pwd):/backup alpine tar xzf /backup/vault_backup.tar.gz -C /data
```

### Nettoyage

```bash
# Supprimer un volume spécifique
docker volume rm moustass_mysql_data

# Supprimer tous les volumes non utilisés
docker volume prune

# Supprimer tous les volumes de l'application (⚠️ ATTENTION)
docker volume rm moustass_mysql_data moustass_vault_data moustass_vault_logs moustass_video_storage
```

---

## 🐳 Gestion des images

### Lister et supprimer

```bash
# Lister toutes les images
docker images

# Lister les images de l'application
docker images | grep moustass

# Supprimer une image
docker rmi moustass/video-moustass-backend:latest

# Supprimer les images non utilisées
docker image prune

# Supprimer toutes les images non utilisées (⚠️ ATTENTION)
docker image prune -a
```

### Tag et push

```bash
# Tag d'une image
docker tag moustass/video-moustass-backend:latest votre_username/video-moustass-backend:1.0.0

# Push sur Docker Hub
docker push votre_username/video-moustass-backend:1.0.0

# Pull depuis Docker Hub
docker pull votre_username/video-moustass-backend:1.0.0
```

---

## 🔐 Docker Hub

### Connexion

```bash
# Connexion interactive
docker login

# Connexion avec credentials
docker login -u votre_username -p votre_password

# Déconnexion
docker logout
```

### Publication complète

```bash
# Script automatique (recommandé)
# Windows
.\publish-dockerhub.ps1

# Linux/Mac
chmod +x publish-dockerhub.sh
./publish-dockerhub.sh

# Manuel
docker compose build
docker tag moustass/video-moustass-backend:latest votre_username/video-moustass-backend:1.0.0
docker tag moustass/video-moustass-frontend:latest votre_username/video-moustass-frontend:1.0.0
docker push votre_username/video-moustass-backend:1.0.0
docker push votre_username/video-moustass-frontend:1.0.0
```

---

## 🧹 Nettoyage

### Nettoyage léger

```bash
# Supprimer les conteneurs arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Supprimer les réseaux non utilisés
docker network prune
```

### Nettoyage complet (⚠️ ATTENTION)

```bash
# Supprimer TOUT (conteneurs, images, volumes, réseaux)
docker system prune -a --volumes

# Avec confirmation
docker system prune -a --volumes --force

# Voir l'espace utilisé
docker system df
```

---

## 🔍 Debugging

### Variables d'environnement

```bash
# Voir toutes les variables d'environnement
docker exec moustass-backend env

# Filtrer les variables
docker exec moustass-backend env | grep -E "DB_|VAULT_|GOOGLE_"

# Voir une variable spécifique (Linux/Mac)
docker exec moustass-backend env | grep DB_HOST

# Windows PowerShell
docker exec moustass-backend env | Select-String "DB_HOST"
```

### Réseau

```bash
# Lister les réseaux
docker network ls

# Inspecter le réseau de l'application
docker network inspect moustass-network

# Voir les conteneurs connectés
docker network inspect moustass-network -f '{{json .Containers}}' | jq
```

### Processus

```bash
# Voir les processus d'un conteneur
docker top moustass-backend

# Statistiques en temps réel
docker stats moustass-backend

# Voir les ports exposés
docker port moustass-backend
docker port moustass-frontend
```

### Health checks

```bash
# Vérifier le health check du backend
curl http://localhost:8082/actuator/health

# Vérifier le health check du frontend
curl http://localhost:3000

# Vérifier Vault
curl http://localhost:8200/v1/sys/health

# Vérifier MySQL
docker exec moustass-mysql mysqladmin ping -h localhost -u root -p
```

---

## 🚨 Dépannage

### Problèmes de démarrage

```bash
# Voir les logs d'erreur
docker compose logs | grep -i error
docker compose logs | grep -i exception

# Redémarrer proprement
docker compose down
docker compose up -d

# Reconstruire et redémarrer
docker compose down
docker compose build --no-cache
docker compose up -d
```

### Problèmes de port

```bash
# Windows - Voir les ports utilisés
netstat -ano | findstr ":3000"
netstat -ano | findstr ":8082"
netstat -ano | findstr ":8200"
netstat -ano | findstr ":3307"

# Tuer un processus
taskkill /PID <PID> /F

# Linux/Mac - Voir les ports utilisés
lsof -i :3000
lsof -i :8082
lsof -i :8200
lsof -i :3307

# Tuer un processus
kill -9 <PID>
```

### Problèmes de volume

```bash
# Vérifier l'espace disque
docker system df

# Voir les volumes orphelins
docker volume ls -f dangling=true

# Supprimer les volumes orphelins
docker volume prune
```

### Reset complet

```bash
# ⚠️ ATTENTION : Supprime TOUT (conteneurs, volumes, images)
docker compose down -v
docker system prune -a --volumes --force

# Redémarrer
docker compose up --build -d
```

---

## 📝 Tests rapides

### Test infrastructure

```bash
# État des services
docker compose ps

# Health checks
curl http://localhost:8082/actuator/health
curl http://localhost:3000
curl http://localhost:8200/v1/sys/health
```

### Test base de données

```bash
# Connexion
docker exec -it moustass-mysql mysql -u root -p

# Requêtes
SHOW DATABASES;
USE moustass_video;
SHOW TABLES;
SELECT COUNT(*) FROM users;
exit;
```

### Test Vault

```bash
# Status
curl http://localhost:8200/v1/sys/health | jq

# Clé Transit
docker exec -it moustass-vault sh -c 'export VAULT_TOKEN=dev-only-token-change-in-prod && vault read transit/keys/video-dek'
```

### Test API

```bash
# Health check
curl http://localhost:8082/api/users/health

# Liste des utilisateurs (nécessite un token)
curl http://localhost:8082/api/users

# Inscription
curl -X POST http://localhost:8082/api/users/inscription \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "mail": "test@example.com",
    "password": "Test1234!"
  }'
```

---

## 🎯 Workflows courants

### Développement quotidien

```bash
# 1. Démarrer
docker compose up -d

# 2. Voir les logs
docker compose logs -f

# 3. Modifier le code

# 4. Rebuild et redémarrer
docker compose up --build -d

# 5. Arrêter
docker compose down
```

### Mise à jour de l'application

```bash
# 1. Pull du code
git pull

# 2. Arrêter
docker compose down

# 3. Rebuild
docker compose build --no-cache

# 4. Redémarrer
docker compose up -d

# 5. Vérifier
docker compose ps
docker compose logs -f
```

### Changement de configuration

```bash
# 1. Modifier le .env

# 2. Redémarrer les services
docker compose down
docker compose up -d

# 3. Vérifier les variables
docker exec moustass-backend env
```

### Backup avant mise à jour

```bash
# 1. Backup MySQL
docker exec moustass-mysql mysqldump -u root -p moustass_video > backup.sql

# 2. Backup Vault
docker run --rm -v moustass_vault_data:/data -v $(pwd):/backup alpine tar czf /backup/vault_backup.tar.gz -C /data .

# 3. Mise à jour
docker compose down
docker compose build --no-cache
docker compose up -d

# 4. Vérifier
docker compose ps
docker compose logs -f
```

---

## 📚 Ressources

### Documentation officielle

- Docker : https://docs.docker.com/
- Docker Compose : https://docs.docker.com/compose/
- MySQL : https://hub.docker.com/_/mysql
- Vault : https://www.vaultproject.io/docs
- Nginx : https://nginx.org/en/docs/

### Documentation du projet

- `README_DOCKER.md` : Documentation complète
- `DEMARRAGE_RAPIDE_DOCKER.md` : Guide de démarrage
- `TESTS_DOCKER.md` : Guide de tests
- `DOCKER_SUMMARY.md` : Récapitulatif

---

## 🆘 En cas de problème

1. **Consulter les logs**
   ```bash
   docker compose logs -f
   ```

2. **Vérifier l'état**
   ```bash
   docker compose ps
   ```

3. **Redémarrer proprement**
   ```bash
   docker compose down
   docker compose up -d
   ```

4. **Consulter la documentation**
   - `README_DOCKER.md` : Section Dépannage

5. **Reset complet (dernier recours)**
   ```bash
   docker compose down -v
   docker compose up --build -d
   ```

---

**Référence rapide toujours à portée de main ! 📖**
