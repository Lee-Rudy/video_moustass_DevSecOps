package com.example.auth.inscription.ports.out;

public interface UserKeyVaultPort  
{
    //Crée une clé asymétrique côté Vault Transit
    void createSigningKey(String vaultKeyName);

    // Exporte (récupère) la clé publique associée
    String exportPublicKey(String vaultKeyName);
    
}
