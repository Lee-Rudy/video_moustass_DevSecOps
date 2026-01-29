# 🧪 Guide : Tester OAuth2 avec plusieurs comptes Google

## ✅ **Modification effectuée**

J'ai ajouté une configuration OAuth2 personnalisée qui **force** Google à afficher l'écran de sélection de compte à chaque connexion.

**Fichier créé :** `src/main/java/com/example/auth/config/OAuth2Config.java`

---

## 📧 **IMPORTANT : Comprendre l'email SMTP vs Email utilisateur**

### **Il y a 2 types d'emails différents :**

```
┌─────────────────────────────────────────────────────────────┐
│  1. EMAIL SMTP (Serveur qui ENVOIE les emails)             │
│     spring.mail.username = brunerleerudy@gmail.com          │
│     spring.mail.password = jmlm zwbc hwej wilb              │
│                                                             │
│     Rôle : Serveur d'envoi d'emails (comme noreply@...)     │
│     NE CHANGE JAMAIS - C'est votre serveur SMTP             │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ ENVOIE un email à ↓
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  2. EMAIL UTILISATEUR OAUTH2 (Qui REÇOIT le code MFA)      │
│     Déterminé par : Le compte Google que vous sélectionnez  │
│                                                             │
│     Exemples :                                              │
│     - Si vous choisissez john@gmail.com → code envoyé à john│
│     - Si vous choisissez alice@gmail.com → code envoyé à alice│
│     - Si vous choisissez test@gmail.com → code envoyé à test│
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 **Comment tester avec plusieurs comptes**

### **Option 1 : Navigation privée (RECOMMANDÉ)**

1. **Fermez** tous les onglets de votre navigateur principal
2. **Ouvrez** une fenêtre de **navigation privée** (Ctrl+Shift+N sur Chrome)
3. Allez sur [http://localhost:5173](http://localhost:5173)
4. Cliquez "Se connecter avec Google"
5. **Vous devriez voir** tous vos comptes Google
6. **Sélectionnez** le compte que vous voulez tester

### **Option 2 : Vider le cache de session Google**

1. Allez sur [https://accounts.google.com](https://accounts.google.com)
2. Cliquez sur votre photo de profil
3. Cliquez sur **"Se déconnecter"**
4. Retournez sur [http://localhost:5173](http://localhost:5173)
5. Cliquez "Se connecter avec Google"
6. **Vous verrez** tous vos comptes Google

### **Option 3 : Tester avec un autre navigateur**

Si vous avez testé sur Chrome, essayez sur :
- Firefox
- Edge
- Safari

---

## 🧪 **Test complet - Étape par étape**

### **Scénario de test :**

#### **Test 1 : Avec brunerleerudy@gmail.com (compte existant)**

1. **Navigation privée** : Ctrl+Shift+N
2. Allez sur [http://localhost:5173](http://localhost:5173)
3. Cliquez "Se connecter avec Google"
4. Google affiche : "Choisir un compte"
5. **Sélectionnez** `brunerleerudy@gmail.com`
6. **Vérifiez l'email** de `brunerleerudy@gmail.com`
7. **Vous devez recevoir** le code MFA
8. Saisissez le code → Connexion réussie ✅

#### **Test 2 : Avec un autre compte Google (nouveau compte)**

1. **Nouvelle navigation privée** : Ctrl+Shift+N
2. Allez sur [http://localhost:5173](http://localhost:5173)
3. Cliquez "Se connecter avec Google"
4. Google affiche : "Choisir un compte"
5. **Cliquez** "Utiliser un autre compte"
6. **Connectez-vous** avec un autre email Google (ex: `test@gmail.com`)
7. Le système va :
   - Créer automatiquement un compte pour `test@gmail.com`
   - Générer un code MFA
   - **Envoyer le code à `test@gmail.com`** (PAS à brunerleerudy@gmail.com)
8. **Vérifiez l'email** de `test@gmail.com`
9. Saisissez le code → Connexion réussie ✅

---

## 🔍 **Vérifier dans les logs à quel email le code est envoyé**

Regardez cette ligne dans les logs :

```
INFO: Utilisateur OAuth2 authentifié - Email: XXXXXX@gmail.com
INFO: Envoi du code MFA à l'adresse : XXXXXX@gmail.com
```

Le code est envoyé à **cet email-là**, pas à `brunerleerudy@gmail.com` (sauf si vous vous connectez avec brunerleerudy).

---

## 📊 **Flux correct**

```
Vous cliquez "Se connecter avec Google"
    ↓
Google affiche : "Choisir un compte"
    - brunerleerudy@gmail.com
    - autreemail@gmail.com  
    - test@gmail.com
    - Utiliser un autre compte
    ↓ (vous sélectionnez test@gmail.com)
Backend reçoit : email = "test@gmail.com"
    ↓
Backend génère code MFA : "123456"
    ↓
Serveur SMTP (brunerleerudy@gmail.com) ENVOIE
    FROM: brunerleerudy@gmail.com
    TO: test@gmail.com  ← Le compte que VOUS avez sélectionné
    SUJET: Code MFA
    CORPS: Votre code est 123456
    ↓
test@gmail.com REÇOIT l'email
    ↓
Vous ouvrez l'email de test@gmail.com
    ↓
Vous saisissez 123456
    ↓
Connexion réussie !
```

---

## 🎯 **Pourquoi spring.mail.username ne doit PAS changer ?**

`spring.mail.username=brunerleerudy@gmail.com` est le **serveur SMTP**.

**Analogie :**
- C'est comme le serveur email de votre entreprise : `noreply@votreentreprise.com`
- Ce serveur **envoie** des emails à tous les clients
- Les clients **reçoivent** sur LEUR email personnel

**Dans votre cas :**
- `brunerleerudy@gmail.com` = Serveur SMTP (ENVOIE les codes MFA)
- N'importe quel compte Google = REÇOIT le code MFA

---

## 🔧 **Actions à faire maintenant**

### **1. Redémarrez l'application**

```bash
# Arrêtez (Ctrl+C)
mvn spring-boot:run
```

### **2. Testez en navigation privée**

```bash
# Chrome/Edge
Ctrl+Shift+N

# Firefox  
Ctrl+Shift+P
```

### **3. Vérifiez l'écran de sélection**

Vous devriez maintenant voir :

```
┌────────────────────────────────────┐
│  Choisir un compte                 │
├────────────────────────────────────┤
│  👤 brunerleerudy@gmail.com        │
│  👤 autreemail@gmail.com            │
│  ➕ Utiliser un autre compte       │
└────────────────────────────────────┘
```

### **4. Sélectionnez un compte différent**

- Si vous sélectionnez `autreemail@gmail.com`
- Le code MFA sera envoyé à `autreemail@gmail.com`
- **VÉRIFIEZ l'email de `autreemail@gmail.com`**, pas de brunerleerudy

---

## 🐛 **Si l'écran de sélection ne s'affiche toujours pas**

### **Solution 1 : Vider le cache Google**

1. Allez sur [https://myaccount.google.com/permissions](https://myaccount.google.com/permissions)
2. Trouvez "Moustass Video" ou votre application
3. Cliquez sur **"Supprimer l'accès"**
4. Retestez la connexion

### **Solution 2 : Déconnectez-vous de Google**

1. Allez sur [https://accounts.google.com](https://accounts.google.com)
2. Cliquez sur votre photo
3. Cliquez **"Se déconnecter de tous les comptes"**
4. Retestez la connexion

### **Solution 3 : Utilisez l'URL directe**

Au lieu de cliquer sur le bouton, testez directement :

```
http://localhost:8082/oauth2/authorization/google
```

Cela devrait forcer l'affichage de la sélection de compte.

---

## 📝 **Vérification dans la base de données**

Pour voir tous les comptes créés :

```sql
SELECT id, name, mail, oauth_provider, oauth_provider_id, created_at 
FROM users 
WHERE oauth_provider = 'google';
```

Chaque compte Google différent aura un `oauth_provider_id` différent.

---

## ✅ **Résumé**

- ✅ **NE PAS** modifier `spring.mail.username` - c'est le serveur SMTP
- ✅ **Redémarrer** l'application
- ✅ **Tester en navigation privée**
- ✅ **Sélectionner** différents comptes Google
- ✅ **Vérifier l'email du compte sélectionné**, pas brunerleerudy

Le code MFA est envoyé à l'email **du compte Google que vous sélectionnez**, pas à `spring.mail.username` ! 🎯

---

**Testez en navigation privée et vous verrez tous vos comptes Google !** 🚀
