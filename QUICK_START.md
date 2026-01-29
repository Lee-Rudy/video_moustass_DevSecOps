# 🚀 Quick Start - OAuth2 + MFA

Guide de démarrage rapide pour tester l'authentification OAuth2 + MFA en 5 minutes.

---

## ⚡ Démarrage rapide (5 minutes)

### Étape 1 : Copier les variables d'environnement

```bash
cp .env.example .env
```

### Étape 2 : Obtenir les credentials Google OAuth2

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créez un projet (ou sélectionnez un existant)
3. Activez **Google+ API**
4. Créez des credentials **OAuth 2.0 Client ID**
5. Configurez :
   - **Authorized redirect URIs** : `http://localhost:8082/login/oauth2/code/google`
6. Copiez **Client ID** et **Client Secret**

### Étape 3 : Configurer Gmail pour l'envoi d'emails

1. Activez l'authentification à deux facteurs sur votre compte Google
2. Générez un mot de passe d'application :
   - [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
3. Copiez le mot de passe généré

### Étape 4 : Remplir le fichier .env

Éditez `.env` et remplissez ces valeurs essentielles :

```bash
# OAuth2 Google
GOOGLE_CLIENT_ID=votre-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=votre-client-secret

# Email SMTP (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-application

# Base de données
DB_HOST=localhost
DB_PORT=3306
DB_NAME=moustass_video
DB_USER=root
DB_PASSWORD=root

# JWT Secret (générez-en un fort)
JWT_SECRET=VotreSuperSecretJWTQuiFaitAuMoins32Caracteres!

# Vault
VAULT_TOKEN=votre-vault-token
```

### Étape 5 : Exécuter la migration SQL

```bash
mysql -u root -p moustass_video < database/migrations/V2__add_oauth2_mfa_support.sql
```

Ou manuellement :

```sql
-- Colonnes OAuth2
ALTER TABLE users
ADD COLUMN oauth_provider VARCHAR(50) NULL,
ADD COLUMN oauth_provider_id VARCHAR(255) NULL,
ADD INDEX idx_oauth_provider (oauth_provider, oauth_provider_id);

-- Table MFA
CREATE TABLE IF NOT EXISTS mfa_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used TINYINT(1) NOT NULL DEFAULT 0,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_session_token (session_token),
    CONSTRAINT fk_mfa_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Étape 6 : Démarrer l'application

**Terminal 1 - Backend :**

```bash
mvn spring-boot:run
```

**Terminal 2 - Frontend :**

```bash
cd front/react-api-app
npm install
npm run dev
```

### Étape 7 : Tester OAuth2 + MFA

1. Ouvrez [http://localhost:5173](http://localhost:5173)
2. Cliquez sur **"Se connecter avec Google"**
3. Sélectionnez votre compte Google
4. Attendez l'email avec le code MFA (vérifiez le spam)
5. Saisissez le code à 6 chiffres dans le modal
6. Vous devriez être redirigé vers le dashboard

---

## ✅ Checklist de vérification

- [ ] Fichier `.env` configuré avec tous les credentials
- [ ] Google OAuth2 credentials créés et configurés
- [ ] Gmail SMTP configuré avec mot de passe d'application
- [ ] Migration SQL exécutée (tables `mfa_codes` créée)
- [ ] Backend démarré sur port 8082
- [ ] Frontend démarré sur port 5173
- [ ] Bouton "Se connecter avec Google" cliquable
- [ ] Redirection vers Google fonctionne
- [ ] Email avec code MFA reçu
- [ ] Modal MFA s'affiche
- [ ] Code MFA valide avec succès

---

## 🧪 Tester les différents scénarios

### Scénario 1 : Nouvel utilisateur OAuth2

1. Utilisez un compte Google qui n'existe pas dans la base de données
2. Le système devrait :
   - Vous rediriger vers Google
   - Créer automatiquement votre compte
   - Envoyer un code MFA par email
   - Afficher le modal MFA
   - Vous connecter après validation du code

### Scénario 2 : Utilisateur OAuth2 existant

1. Utilisez un compte Google déjà enregistré
2. Le système devrait :
   - Vous rediriger vers Google
   - Trouver votre compte existant
   - Envoyer un nouveau code MFA
   - Afficher le modal MFA
   - Vous connecter après validation du code

### Scénario 3 : Connexion classique (sans MFA)

1. Utilisez le formulaire email/password
2. Le système devrait :
   - Vérifier vos credentials
   - Vous connecter SANS demander de code MFA
   - Vous rediriger vers le dashboard

### Scénario 4 : Code MFA expiré

1. Attendez 10 minutes après réception du code
2. Essayez de valider
3. Le système devrait afficher : "Code MFA invalide ou expiré"
4. Cliquez sur "Renvoyer le code"
5. Validez avec le nouveau code

### Scénario 5 : Mauvais code MFA

1. Saisissez un code incorrect
2. Le système devrait afficher : "Code MFA invalide ou expiré"
3. Réessayez avec le bon code

---

## 🐛 Dépannage rapide

### Backend ne démarre pas

**Vérifiez :**

```bash
# MySQL est-il démarré ?
sudo systemctl status mysql

# Le port 8082 est-il libre ?
lsof -i :8082

# Les variables d'environnement sont-elles chargées ?
echo $GOOGLE_CLIENT_ID
```

### Email non reçu

**Vérifiez :**

1. Le dossier spam
2. Les logs backend : `tail -f logs/application.log`
3. Les credentials SMTP dans `.env`
4. Que vous utilisez un mot de passe d'application Gmail (pas votre mot de passe normal)

### "Redirect URI mismatch"

**Solution :**

1. Dans Google Cloud Console, l'URI doit être EXACTEMENT :
   ```
   http://localhost:8082/login/oauth2/code/google
   ```
2. Pas de slash "/" à la fin
3. Tout en minuscules
4. Port 8082 (pas 5173)

### Modal MFA ne s'affiche pas

**Vérifiez :**

1. La console du navigateur (F12) pour les erreurs
2. Que vous êtes bien redirigé après l'authentification Google
3. Que l'email avec le code a été envoyé (logs backend)

### "CORS error"

**Solution :**

Vérifiez dans `application.properties` :

```properties
app.frontend.origin=http://localhost:5173
```

---

## 📊 Vérifier le coverage des tests

```bash
# Exécuter les tests avec rapport de couverture
mvn clean test jacoco:report

# Ouvrir le rapport
# Mac
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

**Coverage attendu : 81%+** ✅

---

## 📚 Documentation complète

- 📖 **[OAUTH2_MFA_SETUP.md](./OAUTH2_MFA_SETUP.md)** : Guide complet de configuration
- 📖 **[README_OAUTH_MFA.md](./README_OAUTH_MFA.md)** : Résumé de l'implémentation

---

## 🎯 Prochaines étapes

Une fois que tout fonctionne :

1. **Testez tous les scénarios** ci-dessus
2. **Vérifiez le coverage** des tests (doit être ≥ 81%)
3. **Examinez les logs** pour comprendre le flux
4. **Lisez la documentation complète** pour comprendre l'architecture

---

## 💡 Conseils

- **Ne committez jamais le fichier `.env`** (déjà dans `.gitignore`)
- **Utilisez des secrets forts** en production
- **Testez d'abord en local** avant de déployer
- **Consultez les logs** en cas de problème

---

## ✨ Résultat attendu

Après avoir suivi ce guide, vous devriez pouvoir :

- ✅ Vous connecter avec Google
- ✅ Recevoir un code MFA par email
- ✅ Valider le code dans un modal élégant
- ✅ Être redirigé vers le dashboard
- ✅ Voir votre compte créé automatiquement en base de données

**Bon test !** 🚀
