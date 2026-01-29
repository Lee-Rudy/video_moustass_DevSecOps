# 🔐 Comment activer OAuth2 + MFA

Par défaut, l'application démarre **sans OAuth2** pour permettre un démarrage rapide.

## ✅ **Pour activer OAuth2 + MFA**

### **Étape 1 : Obtenir les credentials Google OAuth2**

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créez un projet OAuth2
3. Obtenez votre **Client ID** et **Client Secret**

Voir le guide complet : [OAUTH2_MFA_SETUP.md](./OAUTH2_MFA_SETUP.md)

### **Étape 2 : Décommenter la configuration OAuth2**

Éditez `src/main/resources/application.properties` et **décommentez** ces lignes :

```properties
# Décommentez ces lignes pour activer OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=${OAUTH2_REDIRECT_URI:http://localhost:8082/login/oauth2/code/google}
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

### **Étape 3 : Définir les variables d'environnement**

**Option A : Variables d'environnement système**

```bash
# Windows PowerShell
$env:GOOGLE_CLIENT_ID="votre-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="votre-client-secret"

# Linux/Mac
export GOOGLE_CLIENT_ID="votre-client-id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="votre-client-secret"
```

**Option B : Directement dans application.properties** (pour le développement uniquement)

```properties
spring.security.oauth2.client.registration.google.client-id=votre-client-id.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=votre-client-secret
```

⚠️ **ATTENTION** : Ne committez JAMAIS vos credentials dans Git !

### **Étape 4 : Configurer l'email SMTP pour MFA**

Décommentez aussi les lignes email dans `application.properties` :

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-application
```

Ou définissez les variables d'environnement :

```bash
$env:MAIL_USERNAME="votre-email@gmail.com"
$env:MAIL_PASSWORD="votre-mot-de-passe-application"
```

### **Étape 5 : Redémarrer l'application**

```bash
mvn spring-boot:run
```

---

## 🎯 **Vérification**

Si OAuth2 est activé, vous verrez dans les logs :

```
Bean 'customOAuth2UserService' created
Bean 'oauth2AuthenticationSuccessHandler' created
```

Si OAuth2 n'est pas activé :

```
Application started without OAuth2 configuration
```

---

## 🚀 **Tester OAuth2 + MFA**

1. Ouvrez [http://localhost:5173](http://localhost:5173)
2. Cliquez sur "Se connecter avec Google"
3. Sélectionnez votre compte Google
4. Vérifiez votre email pour le code MFA
5. Saisissez le code dans le modal

---

## 📚 **Documentation complète**

- 📖 [OAUTH2_MFA_SETUP.md](./OAUTH2_MFA_SETUP.md) - Guide complet de configuration
- 📖 [QUICK_START.md](./QUICK_START.md) - Démarrage rapide
- 📖 [README_OAUTH_MFA.md](./README_OAUTH_MFA.md) - Résumé de l'implémentation

---

## ❓ **Pourquoi OAuth2 est désactivé par défaut ?**

- ✅ Permet un démarrage rapide pour le développement
- ✅ Pas besoin de credentials Google pour tester les autres fonctionnalités
- ✅ Évite les erreurs de configuration lors du premier démarrage
- ✅ La connexion classique (email/password) fonctionne toujours

OAuth2 + MFA est **optionnel** et s'active uniquement quand vous en avez besoin.
