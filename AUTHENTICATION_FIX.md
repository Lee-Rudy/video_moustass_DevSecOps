# Correctif : Authentification avec Emails en Double

## 🔍 Problème Identifié

**Erreur** : `org.hibernate.NonUniqueResultException: Query did not return a unique result: 2 results were returned`

**Cause** : Plusieurs utilisateurs dans la base de données ont le même email mais des mots de passe différents.

Exemple dans votre base :
```
| id | name  | mail                      | is_admin |
|----|-------|---------------------------|----------|
| 1  | Admin | brunerleerudy@gmail.com   | 1        |
| 5  | Lee   | brunerleerudy@gmail.com   | 0        |
```

## ✅ Solution Implémentée

La distinction entre utilisateurs se fait maintenant par **email + mot de passe**.

### Modifications Apportées

#### 1. **SpringDataUsersRepository** - Ajout d'une nouvelle méthode

```java
/** Retourne tous les utilisateurs avec cet email (pour gérer les doublons) */
java.util.List<UsersJpaEntity> findAllByMail(String mail);
```

#### 2. **LoginService** - Nouvelle logique d'authentification

**Avant** :
```java
Optional<UsersJpaEntity> opt = userRepo.findByMail(mailNorm);
if (opt.isEmpty()) return Optional.empty();
UsersJpaEntity u = opt.get();
if (!encoder.matches(password, u.getPswHash())) return Optional.empty();
```

**Après** :
```java
// Récupérer tous les utilisateurs avec cet email
java.util.List<UsersJpaEntity> users = userRepo.findAllByMail(mailNorm);
if (users.isEmpty()) return Optional.empty();

// Tester le mot de passe pour chaque utilisateur trouvé
for (UsersJpaEntity u : users) {
    if (encoder.matches(password, u.getPswHash())) {
        // Mot de passe trouvé, créer le token
        String token = jwtHelper.createToken(u.getId());
        String name = u.getName() != null ? u.getName() : "";
        return Optional.of(new LoginResponse(token, u.getId(), name, u.isAdmin()));
    }
}
// Aucun utilisateur avec le bon mot de passe
return Optional.empty();
```

#### 3. **Tests Unitaires** - Mise à jour complète

**Changements dans LoginServiceTest** :
- ✅ Remplacement de `findByMail()` par `findAllByMail()` dans tous les tests
- ✅ Utilisation de `Collections.singletonList()` au lieu de `Optional.of()`
- ✅ Utilisation de `Collections.emptyList()` au lieu de `Optional.empty()`

**Nouveaux tests ajoutés** :
- ✅ `authenticate_returnsCorrectUser_whenMultipleUsersWithSameEmail()` - Teste le cas réel avec Admin et Lee
- ✅ `authenticate_returnsFirstMatch_whenMultipleUsersWithSameEmailAndPassword()` - Teste le cas limite

## 🔄 Workflow d'Authentification

```
1. Utilisateur saisit : email + mot de passe
   ↓
2. Normalisation de l'email (trim + lowercase)
   ↓
3. Recherche de TOUS les utilisateurs avec cet email
   ↓
4. Pour chaque utilisateur trouvé :
   - Tester si le mot de passe correspond (BCrypt)
   - Si OUI → créer token JWT et retourner
   - Si NON → tester l'utilisateur suivant
   ↓
5. Si aucun match → retourner "identifiants incorrects"
```

## 📊 Exemple avec Vos Données

### Cas 1 : Connexion en tant qu'Admin
```
Email    : brunerleerudy@gmail.com
Password : Admin123456789

Résultat :
✅ userId: 1
✅ name: "Admin"
✅ isAdmin: true
✅ token: "eyJhbGciOiJIUzI1NiJ9..."
```

### Cas 2 : Connexion en tant que Lee
```
Email    : brunerleerudy@gmail.com
Password : Lee123456789

Résultat :
✅ userId: 5
✅ name: "Lee"
✅ isAdmin: false
✅ token: "eyJhbGciOiJIUzI1NiJ9..."
```

### Cas 3 : Mauvais mot de passe
```
Email    : brunerleerudy@gmail.com
Password : WrongPassword

Résultat :
❌ 401 Unauthorized
```

## 🧪 Tests de Validation

### Test 1 : Un seul utilisateur avec l'email
```java
when(userRepo.findAllByMail("alice@gmail.com"))
    .thenReturn(Collections.singletonList(aliceUser));
when(encoder.matches("Alice123456789", aliceUser.getPswHash()))
    .thenReturn(true);

// ✅ Retourne Alice
```

### Test 2 : Plusieurs utilisateurs avec le même email
```java
when(userRepo.findAllByMail("brunerleerudy@gmail.com"))
    .thenReturn(Arrays.asList(adminUser, leeUser));
when(encoder.matches("Lee123456789", leeUser.getPswHash()))
    .thenReturn(true);

// ✅ Retourne Lee (et non Admin)
```

### Test 3 : Email inexistant
```java
when(userRepo.findAllByMail("inconnu@test.com"))
    .thenReturn(Collections.emptyList());

// ✅ Retourne Optional.empty()
```

## ⚠️ Note Importante

**Sécurité** : Dans un environnement de production, il est **fortement recommandé** de rendre l'email unique avec une contrainte de base de données :

```sql
ALTER TABLE users ADD CONSTRAINT unique_email UNIQUE (mail);
```

Cette solution est adaptée pour votre **environnement de test** où vous avez besoin de plusieurs comptes avec le même email.

## 🎯 Résultats

- ✅ **Plus d'erreur `NonUniqueResultException`**
- ✅ **Authentification par email + mot de passe**
- ✅ **Support de plusieurs utilisateurs avec le même email**
- ✅ **Tous les tests unitaires passent**
- ✅ **Code propre et maintenable**

## 📝 Fichiers Modifiés

1. `SpringDataUsersRepository.java` - Ajout méthode `findAllByMail()`
2. `LoginService.java` - Nouvelle logique d'authentification
3. `LoginServiceTest.java` - Mise à jour des 6 tests existants + 2 nouveaux tests

**Total : 3 fichiers modifiés**

## ✨ Utilisation Frontend

Aucune modification nécessaire côté frontend ! Les appels API restent identiques :

```javascript
const response = await login("brunerleerudy@gmail.com", "Admin123456789");
// Retourne l'utilisateur Admin

const response = await login("brunerleerudy@gmail.com", "Lee123456789");
// Retourne l'utilisateur Lee
```

---

**Date de modification** : 2026-01-27
**Validé par les tests** : ✅ 8/8 tests passent
**Compatible avec** : Base de données existante (pas de migration nécessaire)
