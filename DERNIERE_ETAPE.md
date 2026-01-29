# ⚡ DERNIÈRE ÉTAPE - 2 minutes !

## 🎯 **Il ne manque qu'UNE chose : votre mot de passe Gmail**

---

## **Étape 1 : Générer le mot de passe (1 minute)**

1. Cliquez ici : [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)

2. Si le lien ne fonctionne pas :
   - Activez d'abord la **Validation en deux étapes** sur votre compte Google
   - Puis réessayez le lien

3. Sur la page :
   - Sélectionnez **"Autre (nom personnalisé)"**
   - Tapez **"Moustass Video"**
   - Cliquez **"Générer"**

4. Google affiche un mot de passe comme : **`abcd efgh ijkl mnop`**

5. **COPIEZ-LE** immédiatement (vous ne le reverrez plus)

---

## **Étape 2 : Coller dans application.properties (30 secondes)**

1. Ouvrez : `src/main/resources/application.properties`

2. Trouvez la ligne 36 :
   ```properties
   spring.mail.password=VOTRE-MOT-DE-PASSE-APPLICATION-ICI
   ```

3. Remplacez par votre mot de passe :
   ```properties
   spring.mail.password=abcd efgh ijkl mnop
   ```

4. **Sauvegardez** le fichier (Ctrl+S)

---

## **Étape 3 : Redémarrer et tester (30 secondes)**

1. **Redémarrez** l'application :
   ```bash
   # Arrêtez (Ctrl+C) puis :
   mvn spring-boot:run
   ```

2. **Testez** :
   - Ouvrez [http://localhost:5173](http://localhost:5173)
   - Cliquez "Se connecter avec Google"
   - Sélectionnez un compte
   - **Vérifiez votre email** 📧
   - Saisissez le code MFA
   - **Vous êtes connecté !** 🎉

---

## ✅ **C'est tout !**

OAuth2 + MFA sera **100% fonctionnel** ! 🚀

---

## 📧 **IMPORTANT à comprendre**

```
brunerleerudy@gmail.com  ← EXPÉDITEUR (serveur qui ENVOIE les emails)
                │
                │ envoie un code MFA à
                ▼
n'importe-quel-utilisateur@gmail.com  ← DESTINATAIRE (reçoit le code)
```

Donc si vous testez avec un autre compte Google (ex: `test@gmail.com`), le code sera envoyé à `test@gmail.com`, pas à `brunerleerudy@gmail.com`.

---

## 🎊 **Bon test !**

Vous êtes à **2 minutes** d'avoir OAuth2 + MFA fonctionnel ! ⏱️
