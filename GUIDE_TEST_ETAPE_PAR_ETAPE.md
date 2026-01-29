# 🧪 Guide de test OAuth2 + MFA - Étape par étape

## 🎯 **But : Tester avec plusieurs comptes Google**

---

## 📋 **Checklist avant de commencer**

- [ ] Application backend démarrée (`mvn spring-boot:run`)
- [ ] Frontend démarré (`npm run dev`)
- [ ] Migration SQL exécutée (table `mfa_codes` créée)
- [ ] Mot de passe Gmail configuré dans `application.properties`
- [ ] Paramètre `prompt=select_account` ajouté ✅
- [ ] Configuration OAuth2Config.java créée ✅

---

## 🚀 **TEST 1 : Avec votre compte principal (brunerleerudy@gmail.com)**

### **Étape 1 : Redémarrer l'application**

```bash
# Arrêtez l'application (Ctrl+C)
mvn spring-boot:run
```

Attendez que vous voyiez :
```
Started AuthApplication in X.XXX seconds
```

### **Étape 2 : Ouvrir le frontend**

1. Ouvrez [http://localhost:5173](http://localhost:5173)
2. Vous voyez la page de login

### **Étape 3 : Cliquer "Se connecter avec Google"**

1. Cliquez sur le bouton "Se connecter avec Google"
2. **Vous êtes redirigé vers Google**

### **Étape 4 : Écran de sélection Google**

Vous devriez voir :

```
┌─────────────────────────────────────┐
│  Se connecter avec Google           │
├─────────────────────────────────────┤
│  Choisir un compte                  │
│                                     │
│  👤 brunerleerudy@gmail.com         │
│  👤 Autre compte (si vous en avez)  │
│  ➕ Utiliser un autre compte        │
└─────────────────────────────────────┘
```

**Sélectionnez** : `brunerleerudy@gmail.com`

### **Étape 5 : Vérifiez les logs backend**

```
INFO: Authentification OAuth2 - Email: brunerleerudy@gmail.com
INFO: Code MFA créé pour l'utilisateur ID: 8
INFO: Envoi du code MFA à l'adresse : brunerleerudy@gmail.com
```

### **Étape 6 : Vérifiez votre email**

1. Ouvrez Gmail : [https://mail.google.com](https://mail.google.com)
2. Connectez-vous avec `brunerleerudy@gmail.com`
3. **Cherchez** l'email "Code de vérification MFA - Moustass Video"
4. **Ouvrez** l'email
5. **Copiez** le code à 6 chiffres (ex: `482951`)

### **Étape 7 : Saisissez le code MFA**

1. Retournez sur [http://localhost:5173](http://localhost:5173)
2. Le modal MFA devrait être affiché
3. **Saisissez** les 6 chiffres du code
4. Cliquez "Vérifier"

### **Étape 8 : Connexion réussie**

Vous êtes redirigé vers le dashboard ! ✅

---

## 🧪 **TEST 2 : Avec un compte différent**

### **Étape 1 : Déconnectez-vous**

1. Cliquez sur "Déconnexion" dans l'application
2. Vous revenez sur la page de login

### **Étape 2 : Ouvrir navigation privée**

- **Chrome/Edge** : Appuyez sur `Ctrl+Shift+N`
- **Firefox** : Appuyez sur `Ctrl+Shift+P`

### **Étape 3 : Déconnectez-vous de Google**

1. Allez sur [https://accounts.google.com/Logout](https://accounts.google.com/Logout)
2. Cela déconnecte TOUS vos comptes Google

### **Étape 4 : Retestez la connexion**

1. Allez sur [http://localhost:5173](http://localhost:5173)
2. Cliquez "Se connecter avec Google"

### **Étape 5 : Sélection de compte**

Vous devriez maintenant voir :

```
┌─────────────────────────────────────┐
│  Choisir un compte                  │
├─────────────────────────────────────┤
│  👤 brunerleerudy@gmail.com         │
│  👤 autreemail@gmail.com            │
│  ➕ Utiliser un autre compte        │
└─────────────────────────────────────┘
```

**Cliquez** : "Utiliser un autre compte"

### **Étape 6 : Se connecter avec un autre compte**

1. **Connectez-vous** avec un email Google différent
   - Ex: `test@gmail.com` ou `alice@gmail.com`
2. Autorisez l'accès à l'application

### **Étape 7 : Vérifiez les logs**

```
INFO: Authentification OAuth2 - Email: test@gmail.com  ← Différent !
INFO: Création d'un nouveau compte pour OAuth2
INFO: Nouveau compte créé - ID: 9
INFO: Envoi du code MFA à : test@gmail.com  ← Différent !
```

### **Étape 8 : Vérifiez l'email de TEST**

1. Ouvrez Gmail de `test@gmail.com` (PAS brunerleerudy)
2. **Vous devriez voir** l'email avec le code MFA
3. L'email vient de `brunerleerudy@gmail.com` (serveur SMTP)
4. Mais il est envoyé À `test@gmail.com`

### **Étape 9 : Saisissez le code**

1. Copiez le code à 6 chiffres
2. Retournez sur le modal MFA
3. Saisissez le code
4. Connexion réussie avec `test@gmail.com` ! ✅

---

## 📊 **Vérification en base de données**

```sql
SELECT id, name, mail, oauth_provider, created_at 
FROM users 
WHERE oauth_provider = 'google'
ORDER BY id DESC;
```

**Résultat attendu :**

```
| id | name              | mail                  | oauth_provider | created_at          |
|----|-------------------|-----------------------|----------------|---------------------|
| 9  | Test User         | test@gmail.com        | google         | 2026-01-29 18:00:00 |
| 8  | BRUNER LEE RUDY   | brunerleerudy@...     | google         | 2026-01-29 17:43:00 |
```

**Deux comptes différents** avec deux emails différents ! ✅

---

## ❓ **FAQ - Pourquoi je reçois toujours sur brunerleerudy ?**

### **Question :** "Je reçois toujours le code sur brunerleerudy@gmail.com, pourquoi ?"

### **Réponse :** Vous vous connectez TOUJOURS avec ce compte sur Google !

**Pour vérifier :**

Regardez cette ligne dans les logs :

```
INFO: Authentification OAuth2 - Email: brunerleerudy@gmail.com
                                       ↑
                            C'est LE COMPTE que VOUS avez choisi sur Google
```

Si cette ligne dit `brunerleerudy@gmail.com`, c'est que **vous avez cliqué sur brunerleerudy** dans l'écran de sélection Google !

---

## 🔧 **Si l'écran de sélection ne s'affiche pas**

### **Solution 1 : Vider le cache Google**

```bash
# Dans votre navigateur
1. Allez sur : chrome://settings/clearBrowserData
2. Sélectionnez "Cookies et autres données de site"
3. Cliquez "Effacer les données"
4. Retestez
```

### **Solution 2 : Révoquer l'accès**

1. Allez sur [https://myaccount.google.com/permissions](https://myaccount.google.com/permissions)
2. Trouvez votre application
3. Cliquez "Supprimer l'accès"
4. Retestez

### **Solution 3 : Tester directement l'URL OAuth2**

Au lieu du bouton, testez directement :

```
http://localhost:8082/oauth2/authorization/google
```

Cela devrait forcer l'affichage de la sélection.

---

## ✅ **Ce qui devrait se passer après correction**

```
Vous (navigation privée)
    ↓
Cliquez "Se connecter avec Google"
    ↓
Google affiche : "Choisir un compte"
    ↓
Vous sélectionnez : alice@gmail.com
    ↓
Logs backend : "Email: alice@gmail.com"
    ↓
Logs backend : "Envoi à : alice@gmail.com"
    ↓
Vous ouvrez l'email d'Alice
    ↓
Vous voyez le code MFA
    ↓
Connexion réussie !
```

---

## 🎯 **Résumé**

| Quoi | Valeur | Où |
|------|--------|-----|
| **Serveur SMTP** | `brunerleerudy@gmail.com` | `application.properties` (NE CHANGE JAMAIS) |
| **Compte utilisateur** | N'importe quel compte Google | Sélection Google (CHANGE à chaque test) |
| **Email qui reçoit** | Le compte que VOUS sélectionnez | L'email du compte Google choisi |

---

## 🚀 **Testez MAINTENANT**

1. **Redémarrez** : `mvn spring-boot:run`
2. **Déconnectez-vous** de Google : [accounts.google.com/Logout](https://accounts.google.com/Logout)
3. **Navigation privée** : Ctrl+Shift+N
4. **Connectez** avec un **autre compte Google**
5. **Vérifiez l'email de CET autre compte**
6. **Vous verrez** le code MFA !

**Le système fonctionne parfaitement !** Vous devez juste tester avec différents comptes Google. 🎉
