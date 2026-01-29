# 🚀 Démarrage Rapide Docker

Guide ultra-simplifié pour démarrer l'application avec Docker en 5 minutes.

---

## ⚡ Démarrage Express

### 1. Préparation (une seule fois)

```bash
# 1. Copier le fichier d'environnement
copy .env.docker .env

# 2. Éditer le fichier .env et remplacer au minimum :
#    - DOCKER_USERNAME=votre_username_dockerhub
#    - SMTP_USERNAME=votre_email@gmail.com
#    - SMTP_PASSWORD=votre_mot_de_passe_application
#    - GOOGLE_CLIENT_ID=votre_client_id
#    - GOOGLE_CLIENT_SECRET=votre_client_secret
```

### 2. Lancement

```bash
# Arrêter les services locaux (MySQL, Vault, etc.)
# Puis lancer Docker :
docker compose up -d
```

### 3. Vérification

```bash
# Voir les logs
docker compose logs -f

# Vérifier l'état
docker compose ps
```

### 4. Accès

- **Frontend** : http://localhost:3000
- **Backend** : http://localhost:8082
- **Vault** : http://localhost:8200

---

## 📦 Publication sur Docker Hub

```bash
# Windows
.\publish-dockerhub.ps1

# Linux/Mac
chmod +x publish-dockerhub.sh
./publish-dockerhub.sh
```

Le script vous demandera :
1. Votre username Docker Hub
2. La version (ex: 1.0.0 ou appuyez sur Entrée pour "latest")
3. Confirmation

Ensuite, il fera automatiquement :
- ✅ Build des images
- ✅ Tag des images
- ✅ Push sur Docker Hub

---

## 🛑 Arrêt de l'application

```bash
# Arrêter les conteneurs (les données persistent)
docker compose down

# Arrêter ET supprimer les données
docker compose down -v
```

---

## 🔄 Redémarrage

```bash
# Redémarrer après un arrêt
docker compose up -d

# Redémarrer avec rebuild
docker compose up --build -d
```

---

## 📋 Commandes essentielles

```bash
# Voir les logs en temps réel
docker compose logs -f

# Voir les logs d'un service
docker compose logs -f backend

# Vérifier l'état des services
docker compose ps

# Redémarrer un service
docker compose restart backend

# Accéder à MySQL
docker exec -it moustass-mysql mysql -u root -p

# Accéder au backend
docker exec -it moustass-backend sh
```

---

## ❓ Problèmes courants

### Port déjà utilisé

```bash
# Windows - Trouver et tuer le processus
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>
```

### MySQL ne démarre pas

```bash
# Voir les logs
docker compose logs mysql

# Redémarrer
docker compose restart mysql
```

### Vault non initialisé

```bash
# Voir les logs
docker compose logs vault-init

# Redémarrer l'initialisation
docker compose restart vault-init
```

### Backend ne se connecte pas

```bash
# Vérifier les variables d'environnement
docker exec moustass-backend env | grep -E "DB_|VAULT_|GOOGLE_"

# Redémarrer
docker compose restart backend
```

---

## 🎯 Checklist de démarrage

- [ ] Docker Desktop est démarré
- [ ] Le fichier `.env` est configuré
- [ ] Les services locaux (MySQL, etc.) sont arrêtés
- [ ] Les ports 3000, 8082, 8200, 3307 sont libres
- [ ] `docker compose up -d` a été exécuté
- [ ] Tous les services sont "healthy" (`docker compose ps`)
- [ ] L'application est accessible sur http://localhost:3000

---

## 📚 Documentation complète

Pour plus de détails, consultez `README_DOCKER.md` qui contient :
- Architecture détaillée
- Configuration avancée
- Gestion des volumes
- Sécurité en production
- Dépannage complet

---

## 🆘 Besoin d'aide ?

1. Consultez les logs : `docker compose logs -f`
2. Vérifiez l'état : `docker compose ps`
3. Consultez `README_DOCKER.md` pour le dépannage

---

**Bonne utilisation ! 🐳**
