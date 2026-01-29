# 📧 Configuration Email SMTP pour MFA

OAuth2 est maintenant activé ! Pour que le MFA fonctionne, vous devez configurer l'envoi d'emails.

## 🚀 Option recommandée : Gmail

### **Étape 1 : Activer l'authentification à 2 facteurs sur votre compte Google**

1. Allez sur [myaccount.google.com](https://myaccount.google.com)
2. Cliquez sur **Sécurité** dans le menu de gauche
3. Activez l'**Authentification à 2 facteurs**

### **Étape 2 : Générer un mot de passe d'application**

1. Une fois la 2FA activée, retournez dans **Sécurité**
2. Cherchez "Mots de passe d'application"
3. Cliquez sur **Mots de passe d'application**
   - URL directe : [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
4. Sélectionnez "Mail" dans le menu déroulant
5. Sélectionnez "Autre (nom personnalisé)" et entrez "Moustass Video"
6. Cliquez sur **Générer**
7. **Copiez le mot de passe à 16 caractères** (format : xxxx xxxx xxxx xxxx)

### **Étape 3 : Configurer dans application.properties**

Ouvrez `src/main/resources/application.properties` et modifiez ces lignes :

```properties
# --- Configuration Email (SMTP) ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

Remplacez :
- `votre-email@gmail.com` par votre vraie adresse Gmail
- `xxxx xxxx xxxx xxxx` par le mot de passe d'application généré (gardez ou enlevez les espaces, les deux fonctionnent)

---

## 📝 **Exemple de configuration complète**

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=brunerleerudy@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

---

## ✅ **Vérification**

Une fois configuré, démarrez l'application :

```bash
mvn spring-boot:run
```

Et testez OAuth2 + MFA :

1. Ouvrez [http://localhost:5173](http://localhost:5173)
2. Cliquez sur "Se connecter avec Google"
3. Sélectionnez votre compte Google
4. **Vous devriez recevoir un email** avec le code MFA à 6 chiffres
5. Saisissez le code dans le modal

---

## 🔧 **Alternatives à Gmail**

### **SendGrid**

```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=votre-sendgrid-api-key
```

### **Mailgun**

```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=postmaster@votre-domaine.mailgun.org
spring.mail.password=votre-mailgun-password
```

---

## ⚠️ **Dépannage**

### Erreur : "Authentication failed"

- Vérifiez que la 2FA est activée sur votre compte Google
- Vérifiez que vous utilisez un **mot de passe d'application** (pas votre mot de passe normal)
- Vérifiez que l'email et le mot de passe sont corrects

### Email non reçu

- Vérifiez le dossier **spam**
- Vérifiez que l'application a démarré sans erreur
- Consultez les logs : recherchez "Code MFA envoyé avec succès"

### "Client is not authenticated"

- Le mot de passe d'application est incorrect
- Utilisez le mot de passe généré par Google (16 caractères)

---

## 🎉 **C'est tout !**

Une fois l'email configuré, OAuth2 + MFA sera **100% fonctionnel** !
