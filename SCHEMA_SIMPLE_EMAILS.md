# 📧 Schéma ULTRA-SIMPLE des emails

## 🎯 **Ce que vous devez comprendre en 30 secondes**

---

## **Situation actuelle :**

```
┌─────────────────────────────────────────────────────────────┐
│  SERVEUR SMTP (configuration dans application.properties)   │
│                                                             │
│  spring.mail.username = brunerleerudy@gmail.com             │
│  spring.mail.password = jmlm zwbc hwej wilb                 │
│                                                             │
│  Rôle : Serveur qui ENVOIE les emails                      │
│  Comme : noreply@facebook.com, noreply@amazon.com          │
│                                                             │
│  ⚠️ NE TOUCHEZ JAMAIS CETTE LIGNE !                        │
│  C'est votre serveur d'envoi, pas le compte utilisateur    │
└─────────────────────────────────────────────────────────────┘
```

---

## **Ce qui se passe quand vous testez :**

### **Test 1 : Vous vous connectez avec brunerleerudy@gmail.com**

```
1. Vous cliquez "Se connecter avec Google"
2. Google OAuth2 : "Choisir un compte"
3. VOUS sélectionnez : brunerleerudy@gmail.com
   ↓
Backend reçoit : Email = brunerleerudy@gmail.com
   ↓
Backend génère code : 123456
   ↓
Serveur SMTP (brunerleerudy@gmail.com) ENVOIE
   FROM: brunerleerudy@gmail.com
   TO: brunerleerudy@gmail.com  ← Vous recevez ici
   CODE: 123456
   ↓
Vous ouvrez l'email de brunerleerudy@gmail.com
   ↓
Vous saisissez 123456
   ↓
Connexion réussie !
```

### **Test 2 : Vous vous connectez avec alice@gmail.com**

```
1. Vous cliquez "Se connecter avec Google"
2. Google OAuth2 : "Choisir un compte"
3. VOUS sélectionnez : alice@gmail.com  ← Différent !
   ↓
Backend reçoit : Email = alice@gmail.com
   ↓
Backend génère code : 789012
   ↓
Serveur SMTP (brunerleerudy@gmail.com) ENVOIE
   FROM: brunerleerudy@gmail.com  ← Serveur (toujours le même)
   TO: alice@gmail.com           ← Destinataire (différent !)
   CODE: 789012
   ↓
Alice ouvre l'email de alice@gmail.com  ← PAS brunerleerudy !
   ↓
Alice saisit 789012
   ↓
Connexion réussie !
```

---

## 🔑 **La clé du succès**

### **Pour recevoir le code sur un autre email :**

**IL FAUT SÉLECTIONNER UN AUTRE COMPTE SUR GOOGLE !**

Pas modifier `spring.mail.username` !

---

## 🧪 **Test PRATIQUE**

### **Étape 1 : Ouvrir navigation privée**

- **Chrome/Edge** : Ctrl+Shift+N
- **Firefox** : Ctrl+Shift+P

### **Étape 2 : Tester la connexion**

1. Allez sur [http://localhost:5173](http://localhost:5173)
2. Cliquez "Se connecter avec Google"
3. **Vous voyez quoi ?**

**Cas A : Vous voyez l'écran de sélection ✅**
```
┌────────────────────────────┐
│ Choisir un compte          │
├────────────────────────────┤
│ 👤 brunerleerudy@gmail.com │
│ 👤 alice@gmail.com         │
│ ➕ Utiliser un autre compte│
└────────────────────────────┘
```
→ **PARFAIT !** Sélectionnez alice@gmail.com
→ Le code sera envoyé à alice@gmail.com

**Cas B : Connexion directe sans choix ❌**
```
Redirection directe vers brunerleerudy@gmail.com
(pas d'écran de sélection)
```
→ Google a mis en cache votre session
→ **Solution :** Déconnectez-vous de Google :
   - [https://accounts.google.com/Logout](https://accounts.google.com/Logout)
   - Puis retestez

---

## 📊 **Vérification : Regardez les LOGS**

Quand vous testez, regardez cette ligne dans les logs :

```
INFO: Utilisateur OAuth2 authentifié - Email: XXXXXX@gmail.com
                                               ↑
                                    C'est le compte que VOUS avez choisi sur Google
```

```
INFO: Envoi du code MFA à l'adresse : XXXXXX@gmail.com
                                      ↑
                           Le code est envoyé ICI
```

**Si ces deux lignes disent `brunerleerudy@gmail.com`**, c'est que **vous avez sélectionné brunerleerudy sur Google**, pas à cause de `spring.mail.username` !

---

## 🎯 **Preuve finale**

Pour PROUVER que le système fonctionne :

1. **Déconnectez-vous** de tous vos comptes Google :
   - [https://accounts.google.com/Logout](https://accounts.google.com/Logout)

2. **Navigation privée** : Ctrl+Shift+N

3. Allez sur [http://localhost:5173](http://localhost:5173)

4. Cliquez "Se connecter avec Google"

5. Google demande : "Se connecter avec Google"

6. **Connectez-vous avec un email DIFFÉRENT** (ex: `test@gmail.com`)

7. **Regardez les logs** :
   ```
   INFO: Email: test@gmail.com        ← Le compte que vous avez choisi
   INFO: Envoi à : test@gmail.com     ← Le code est envoyé ICI
   ```

8. **Vérifiez l'email de `test@gmail.com`**

9. **VOUS VERREZ LE CODE MFA !** 🎉

---

## ❌ **Ce que vous ne devez PAS faire**

```
❌ Modifier spring.mail.username à chaque fois
❌ Penser que spring.mail.username = compte utilisateur
❌ Changer le mot de passe pour chaque utilisateur
```

## ✅ **Ce que vous devez faire**

```
✅ Garder spring.mail.username = brunerleerudy@gmail.com
✅ Sélectionner DIFFÉRENTS comptes sur Google
✅ Vérifier l'email du compte que VOUS avez sélectionné
✅ Tester en navigation privée
```

---

## 🎉 **Résumé en image**

```
APPLICATION.PROPERTIES (NE CHANGE JAMAIS)
┌──────────────────────────────────────┐
│ spring.mail.username=                │
│ brunerleerudy@gmail.com              │  Serveur SMTP
└──────────────────────────────────────┘
            │
            │ ENVOIE des emails à ↓
            ▼
UTILISATEURS (CHANGENT À CHAQUE CONNEXION)
┌──────────────────────────────────────┐
│ Alice se connecte → alice@gmail.com  │
│ John se connecte → john@gmail.com    │
│ Test se connecte → test@gmail.com    │
│ Vous se connectez → votre-email      │
└──────────────────────────────────────┘
```

---

## 🚀 **ACTION IMMÉDIATE**

1. **Redémarrez** l'application
   ```bash
   mvn spring-boot:run
   ```

2. **Déconnectez-vous** de Google
   - [https://accounts.google.com/Logout](https://accounts.google.com/Logout)

3. **Navigation privée** : Ctrl+Shift+N

4. **Testez** avec un compte différent

5. **Vérifiez l'email de CE compte**

**Vous verrez que ça fonctionne !** 🎉

---

## 💡 **Note finale**

`spring.mail.username` est comme le serveur email d'une entreprise :
- Amazon envoie depuis `noreply@amazon.com`
- Mais VOUS recevez sur `client@gmail.com`

C'est pareil ici ! 😊

**Testez en navigation privée avec un autre compte et tout sera clair !** 🚀
