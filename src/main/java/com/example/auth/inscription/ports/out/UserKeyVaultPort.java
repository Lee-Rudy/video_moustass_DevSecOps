package com.example.auth.inscription.ports.out;

public interface UserKeyVaultPort {

    void createSigningKey(String vaultKeyName);

    String exportPublicKey(String vaultKeyName);

    /** Chiffre un DEK (32 bytes) avec la clé Transit symétrique video-dek. Retourne le ciphertext (vault:v1:...). */
    String encryptDek(String keyName, byte[] dekPlaintext);

    /** Déchiffre un DEK via Transit. ciphertext au format vault:v1:... */
    byte[] decryptDek(String keyName, String ciphertext);

    /** Signe le hash (SHA-256) de la vidéo avec la clé privée de l'utilisateur (vault_key). inputBase64 = Base64(hash). */
    String sign(String vaultKeyName, String inputBase64);

    /** Vérifie la signature avec la clé (vault_key) de l'expéditeur. Retourne true si valide. */
    boolean verify(String vaultKeyName, String inputBase64, String signature);
}
