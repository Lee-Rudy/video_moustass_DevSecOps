# 🧪 Guide de Tests Docker - Application Vidéo Moustass

Ce guide vous permet de tester systématiquement tous les composants de l'application dans Docker.

---

## 📋 Checklist des Tests

- [ ] Infrastructure (MySQL, Vault)
- [ ] Backend (API, OAuth2, MFA)
- [ ] Frontend (Interface, Navigation)
- [ ] Persistance des données
- [ ] Sécurité et chiffrement
- [ ] Performance et health checks

---

## 🏗️ Tests d'Infrastructure

### Test 1: Vérifier que tous les conteneurs sont démarrés

```bash
docker compose ps
```

**Résultat attendu** : Tous les services doivent avoir le status "Up" et "healthy"

```
NAME                  STATUS              PORTS
moustass-backend      Up (healthy)        0.0.0.0:8082->8082/tcp
moustass-frontend     Up (healthy)        0.0.0.0:3000->80/tcp
moustass-mysql        Up (healthy)        0.0.0.0:3307->3306/tcp
moustass-vault        Up (healthy)        0.0.0.0:8200->8200/tcp
moustass-vault-init   Exited (0)
```

### Test 2: Vérifier MySQL

```bash
# Se connecter à MySQL
docker exec -it moustass-mysql mysql -u root -p

# Entrer le mot de passe (défini dans .env)
# Puis exécuter :
SHOW DATABASES;
USE moustass_video;
SHOW TABLES;
exit;
```

**Résultat attendu** : 
- Base de données `moustass_video` existe
- Tables : `users`, `signature_transactions`, `audit_logs`, `mfa_codes`, `notifications`

### Test 3: Vérifier Vault

```bash
# Vérifier le status de Vault
curl http://localhost:8200/v1/sys/health | jq

# Se connecter au conteneur
docker exec -it moustass-vault sh

# Vérifier la clé Transit
export VAULT_TOKEN=dev-only-token-change-in-prod
vault read transit/keys/video-dek
exit
```

**Résultat attendu** :
- Vault répond "initialized": true, "sealed": false
- La clé `video-dek` existe

### Test 4: Vérifier les volumes persistants

```bash
# Lister les volumes
docker volume ls | grep moustass

# Inspecter le volume MySQL
docker volume inspect moustass_mysql_data

# Inspecter le volume Vault
docker volume inspect moustass_vault_data

# Inspecter le volume vidéos
docker volume inspect moustass_video_storage
```

**Résultat attendu** : 4 volumes existent et ont un "Mountpoint"

---

## 🔌 Tests du Backend

### Test 5: Health Check du Backend

```bash
# Test de santé
curl http://localhost:8082/api/users/health

# Health check Actuator
curl http://localhost:8082/actuator/health | jq
```

**Résultat attendu** : 
```json
{
  "status": "UP"
}
```

### Test 6: Test de la base de données depuis le Backend

```bash
# Créer un utilisateur test
curl -X POST http://localhost:8082/api/users/inscription \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "mail": "test@example.com",
    "password": "Test1234!"
  }'
```

**Résultat attendu** : Code 201 Created avec l'utilisateur créé

### Test 7: Test de connexion à Vault depuis le Backend

```bash
# Voir les logs du backend pour vérifier la connexion Vault
docker compose logs backend | grep -i vault
```

**Résultat attendu** : Pas d'erreurs de connexion à Vault

### Test 8: Test OAuth2 Google

1. Ouvrir http://localhost:3000 dans le navigateur
2. Cliquer sur "Se connecter avec Google"
3. Vérifier la redirection vers Google
4. Se connecter avec un compte Google
5. Vérifier la réception du code MFA par email

**Résultat attendu** : 
- Redirection vers Google fonctionne
- Après connexion, retour sur l'application
- Email MFA reçu

### Test 9: Test MFA (Email)

1. Après la connexion OAuth2, vérifier l'email
2. Entrer le code MFA à 6 chiffres
3. Vérifier l'accès au dashboard

**Résultat attendu** :
- Email reçu avec code à 6 chiffres
- Code valide et fonctionne
- Accès au dashboard accordé

---

## 🎨 Tests du Frontend

### Test 10: Accès au Frontend

```bash
# Test de disponibilité
curl http://localhost:3000

# Test du proxy API
curl http://localhost:3000/api/users/health
```

**Résultat attendu** : 
- Frontend accessible
- Proxy API fonctionne

### Test 11: Test de Navigation

Ouvrir http://localhost:3000 et tester :

1. **Page d'accueil**
   - [ ] Page se charge correctement
   - [ ] Bouton "Se connecter avec Google" visible
   - [ ] Formulaire d'inscription visible

2. **Inscription classique**
   - [ ] Remplir le formulaire
   - [ ] Soumettre
   - [ ] Vérifier la création dans la BDD

3. **Connexion OAuth2**
   - [ ] Cliquer sur "Google"
   - [ ] Redirection fonctionne
   - [ ] Retour après authentification

4. **Dashboard**
   - [ ] Accessible après connexion
   - [ ] Navigation fonctionne
   - [ ] Déconnexion fonctionne

---

## 💾 Tests de Persistance

### Test 12: Test de persistance MySQL

```bash
# 1. Créer un utilisateur via l'interface web
# http://localhost:3000 > Inscription

# 2. Vérifier dans la BDD
docker exec -it moustass-mysql mysql -u root -p -e "SELECT * FROM moustass_video.users;"

# 3. Arrêter les conteneurs
docker compose down

# 4. Redémarrer
docker compose up -d

# 5. Attendre 30 secondes puis revérifier
docker exec -it moustass-mysql mysql -u root -p -e "SELECT * FROM moustass_video.users;"
```

**Résultat attendu** : L'utilisateur existe toujours après le redémarrage

### Test 13: Test de persistance Vault

```bash
# 1. Arrêter les conteneurs
docker compose down

# 2. Redémarrer
docker compose up -d

# 3. Vérifier que la clé existe toujours
docker exec -it moustass-vault sh -c 'export VAULT_TOKEN=dev-only-token-change-in-prod && vault read transit/keys/video-dek'
```

**Résultat attendu** : La clé `video-dek` existe toujours

### Test 14: Test de persistance des vidéos

```bash
# 1. Créer un fichier test dans le volume vidéos
docker exec -it moustass-backend sh -c 'echo "test" > /app/data/videos/test.txt'

# 2. Vérifier
docker exec -it moustass-backend sh -c 'cat /app/data/videos/test.txt'

# 3. Arrêter et redémarrer
docker compose down
docker compose up -d

# 4. Revérifier
docker exec -it moustass-backend sh -c 'cat /app/data/videos/test.txt'
```

**Résultat attendu** : Le fichier existe toujours après le redémarrage

---

## 🔐 Tests de Sécurité et Chiffrement

### Test 15: Test de chiffrement Vault Transit

```bash
# 1. Se connecter à Vault
docker exec -it moustass-vault sh

export VAULT_TOKEN=dev-only-token-change-in-prod
export VAULT_ADDR=http://localhost:8200

# 2. Chiffrer une donnée
vault write transit/encrypt/video-dek plaintext=$(echo "Test de chiffrement" | base64)

# 3. La sortie doit ressembler à : vault:v1:XXXXX...

# 4. Déchiffrer
vault write transit/decrypt/video-dek ciphertext="vault:v1:XXXXX..."

# 5. Décoder le base64
# (copier le plaintext et décoder avec: echo "XXXXX" | base64 -d)

exit
```

**Résultat attendu** : Chiffrement/déchiffrement fonctionne

### Test 16: Test JWT

```bash
# 1. Se connecter via l'API
TOKEN=$(curl -X POST http://localhost:8082/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "mail": "test@example.com",
    "password": "Test1234!"
  }' | jq -r '.token')

echo $TOKEN

# 2. Utiliser le token pour accéder à une ressource protégée
curl http://localhost:8082/api/users \
  -H "Authorization: Bearer $TOKEN"
```

**Résultat attendu** : Token reçu et utilisable

### Test 17: Test CORS

```bash
# Test de requête cross-origin
curl -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS \
  -v \
  http://localhost:8082/api/users/health
```

**Résultat attendu** : Headers CORS présents dans la réponse

---

## 📊 Tests de Performance

### Test 18: Test de charge basique

```bash
# Installer Apache Bench (si pas installé)
# Windows: scoop install apache-bench
# Linux: apt-get install apache2-utils
# Mac: brew install ab

# Test de 100 requêtes avec 10 concurrentes
ab -n 100 -c 10 http://localhost:8082/api/users/health

# Test du frontend
ab -n 100 -c 10 http://localhost:3000/
```

**Résultat attendu** : 
- Taux de réussite : 100%
- Temps de réponse moyen < 500ms

### Test 19: Monitoring des ressources

```bash
# Voir l'utilisation des ressources en temps réel
docker stats

# Voir les ressources d'un conteneur spécifique
docker stats moustass-backend
```

**Résultat attendu** : 
- Backend : < 512MB RAM
- Frontend : < 50MB RAM
- MySQL : < 200MB RAM
- Vault : < 100MB RAM

---

## 🔄 Tests de Redémarrage et Récupération

### Test 20: Test de redémarrage d'un service

```bash
# 1. Arrêter le backend
docker compose stop backend

# 2. Vérifier que le frontend est toujours accessible
curl http://localhost:3000

# 3. Redémarrer le backend
docker compose start backend

# 4. Attendre 30 secondes et tester
curl http://localhost:8082/api/users/health
```

**Résultat attendu** : Le backend redémarre correctement

### Test 21: Test de récupération après crash

```bash
# 1. Tuer brutalement le backend
docker kill moustass-backend

# 2. Attendre le redémarrage automatique (restart: unless-stopped)
sleep 30

# 3. Vérifier
docker compose ps
curl http://localhost:8082/api/users/health
```

**Résultat attendu** : Le conteneur redémarre automatiquement

---

## 🧹 Tests de Nettoyage

### Test 22: Test de suppression des données

```bash
# 1. Arrêter et supprimer tout (y compris les volumes)
docker compose down -v

# 2. Vérifier que les volumes sont supprimés
docker volume ls | grep moustass

# 3. Redémarrer
docker compose up -d

# 4. Vérifier que la BDD est réinitialisée
docker exec -it moustass-mysql mysql -u root -p -e "SELECT * FROM moustass_video.users;"
```

**Résultat attendu** : 
- Volumes supprimés
- Base de données vide après redémarrage

---

## 📝 Rapport de Tests

### Template de rapport

```markdown
# Rapport de Tests Docker - [Date]

## Environnement
- OS: Windows/Linux/Mac
- Docker: [version]
- Docker Compose: [version]

## Résultats

### Infrastructure (Tests 1-4)
- [ ] Test 1: Conteneurs - ✅/❌
- [ ] Test 2: MySQL - ✅/❌
- [ ] Test 3: Vault - ✅/❌
- [ ] Test 4: Volumes - ✅/❌

### Backend (Tests 5-9)
- [ ] Test 5: Health Check - ✅/❌
- [ ] Test 6: Base de données - ✅/❌
- [ ] Test 7: Vault - ✅/❌
- [ ] Test 8: OAuth2 - ✅/❌
- [ ] Test 9: MFA - ✅/❌

### Frontend (Tests 10-11)
- [ ] Test 10: Accès - ✅/❌
- [ ] Test 11: Navigation - ✅/❌

### Persistance (Tests 12-14)
- [ ] Test 12: MySQL - ✅/❌
- [ ] Test 13: Vault - ✅/❌
- [ ] Test 14: Vidéos - ✅/❌

### Sécurité (Tests 15-17)
- [ ] Test 15: Chiffrement - ✅/❌
- [ ] Test 16: JWT - ✅/❌
- [ ] Test 17: CORS - ✅/❌

### Performance (Tests 18-19)
- [ ] Test 18: Charge - ✅/❌
- [ ] Test 19: Ressources - ✅/❌

### Récupération (Tests 20-21)
- [ ] Test 20: Redémarrage - ✅/❌
- [ ] Test 21: Crash - ✅/❌

### Nettoyage (Test 22)
- [ ] Test 22: Suppression - ✅/❌

## Problèmes rencontrés
[Description des problèmes]

## Recommandations
[Améliorations suggérées]
```

---

## 🎯 Critères de Succès

L'application est considérée comme **prête pour la production** si :

- ✅ Tous les conteneurs démarrent et sont "healthy"
- ✅ Les données persistent après un redémarrage
- ✅ OAuth2 et MFA fonctionnent correctement
- ✅ Le chiffrement Vault fonctionne
- ✅ Les health checks répondent positivement
- ✅ La performance est acceptable (< 500ms)
- ✅ Les conteneurs redémarrent automatiquement après un crash
- ✅ Aucune erreur dans les logs

---

## 🆘 Aide au Dépannage

Si un test échoue, consultez :

1. Les logs : `docker compose logs -f [service]`
2. L'état : `docker compose ps`
3. Le fichier `README_DOCKER.md` section Dépannage
4. Les variables d'environnement : `docker exec [container] env`

---

**Bons tests ! 🧪✅**
