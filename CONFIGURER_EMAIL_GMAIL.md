# 📧 Configuration Email Gmail pour MFA - Guide Visuel

## ⚠️ IMPORTANT : Comprendre le système d'email

### **Comment ça marche ?**

```
┌─────────────────────────────────────────────────────────────────┐
│  Serveur SMTP (Compte Gmail de l'application)                  │
│  Email: brunerleerudy@gmail.com                                 │
│  Rôle: ENVOYER les codes MFA                                    │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ Envoie un email avec code MFA
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  Utilisateur OAuth2 (N'importe quel email)                      │
│  Email: n'importe-quel-utilisateur@gmail.com                    │
│  Rôle: RECEVOIR le code MFA                                     │
└─────────────────────────────────────────────────────────────────┘
```

### **Exemple concret :**

1. **Vous** vous connectez avec votre compte Google `john@gmail.com`
2. Le **serveur SMTP** (`brunerleerudy@gmail.com`) **ENVOIE** un email
3. **Vous** recevez l'email à `john@gmail.com` avec le code MFA
4. **Vous** saisissez le code et vous connectez

---

## 🔑 Étape 1 : Générer un mot de passe d'application Gmail

### **1.1 Activez la validation en deux étapes**

1. Allez sur : [https://myaccount.google.com/security](https://myaccount.google.com/security)
2. Cherchez "Validation en deux étapes"
3. Cliquez sur **Activer** si ce n'est pas déjà fait
4. Suivez les instructions

### **1.2 Générez un mot de passe d'application**

1. Une fois la 2FA activée, retournez sur : [https://myaccount.google.com/security](https://myaccount.google.com/security)
2. Cherchez "Mots de passe d'application"
3. Cliquez dessus (ou allez directement sur : [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords))

**Vous verrez un écran comme ceci :**

```
┌─────────────────────────────────────────┐
│  Mots de passe d'application            │
├─────────────────────────────────────────┤
│                                         │
│  Sélectionner l'application et          │
│  l'appareil                              │
│                                         │
│  Application: [Autre (nom personnalisé)]│
│                                         │
│  Nom: [Moustass Video]                  │
│                                         │
│        [Générer]                        │
└─────────────────────────────────────────┘
```

4. Sélectionnez **"Autre (nom personnalisé)"**
5. Entrez **"Moustass Video"**
6. Cliquez sur **"Générer"**

**Google va afficher un mot de passe :**

```
┌─────────────────────────────────────────┐
│  Votre mot de passe d'application est : │
│                                         │
│   ┌───────────────────────────────┐    │
│   │  abcd efgh ijkl mnop          │    │
│   └───────────────────────────────┘    │
│                                         │
│   Copiez ce mot de passe maintenant     │
│   Vous ne le reverrez plus              │
└─────────────────────────────────────────┘
```

5. **COPIEZ** ce mot de passe (les espaces sont optionnels)

---

## 📝 Étape 2 : Configurer dans application.properties

Ouvrez `src/main/resources/application.properties` et trouvez cette ligne :

```properties
spring.mail.password=VOTRE-MOT-DE-PASSE-APPLICATION-ICI
```

Remplacez par le mot de passe que vous venez de copier :

```properties
spring.mail.password=abcd efgh ijkl mnop
```

**Exemple complet :**

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=brunerleerudy@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

---

## 🚀 Étape 3 : Redémarrer l'application

```bash
# Arrêtez l'application actuelle (Ctrl+C dans le terminal)
# Redémarrez
mvn spring-boot:run
```

---

## ✅ Étape 4 : Tester OAuth2 + MFA

1. Ouvrez [http://localhost:5173](http://localhost:5173)
2. Cliquez sur **"Se connecter avec Google"**
3. Sélectionnez **n'importe quel compte Google**
4. **Vérifiez l'email de CE compte** (pas brunerleerudy@gmail.com, mais le compte que vous avez sélectionné)
5. Vous devriez recevoir un email avec un code à 6 chiffres
6. Saisissez le code dans le modal
7. Vous êtes connecté !

---

## 📊 Schéma complet du flux

```
┌──────────────────────────────────────────────────────────────┐
│ 1. Utilisateur clique "Se connecter avec Google"             │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. Utilisateur sélectionne son compte (ex: john@gmail.com)   │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. Backend génère un code MFA (ex: 123456)                   │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. Serveur SMTP (brunerleerudy@gmail.com) ENVOIE l'email     │
│    VERS john@gmail.com                                        │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. John reçoit l'email avec le code 123456                   │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. John saisit le code dans le modal                         │
└──────────────────────────────────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ 7. John est connecté et redirigé vers le dashboard           │
└──────────────────────────────────────────────────────────────┘
```

---

## 🎯 Résumé des corrections

### **Problème 1 : Erreur SSL** ✅ CORRIGÉ
- Ajout de `spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com`
- Ajout de `spring.mail.properties.mail.smtp.ssl.protocols=TLSv1.2`

### **Problème 2 : Email expéditeur** ✅ CORRIGÉ
- Ajout de `helper.setFrom(fromEmail)` dans EmailService

### **Problème 3 : Mot de passe manquant** ⚠️ À FAIRE
- Vous devez générer et configurer votre mot de passe d'application Gmail

---

## 🐛 Dépannage

### Si l'email n'est toujours pas envoyé :

**Option 1 : Vérifier Gmail "Accès moins sécurisé"**

Gmail peut bloquer les connexions. Essayez :
1. Allez sur [https://myaccount.google.com/lesssecureapps](https://myaccount.google.com/lesssecureapps)
2. Activez "Autoriser les applications moins sécurisées" (temporairement pour tester)

**Option 2 : Utiliser un autre email de test**

Si vous avez des problèmes avec Gmail, utilisez Mailtrap pour le développement :

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=votre-username-mailtrap
spring.mail.password=votre-password-mailtrap
```

Créez un compte gratuit sur [https://mailtrap.io](https://mailtrap.io)

---

## ✨ Une fois configuré

OAuth2 + MFA sera **100% fonctionnel** !

- ✅ Connexion avec Google
- ✅ Création automatique de compte
- ✅ Envoi de code MFA par email
- ✅ Validation du code
- ✅ Connexion réussie

**Générez votre mot de passe d'application et testez !** 🚀
