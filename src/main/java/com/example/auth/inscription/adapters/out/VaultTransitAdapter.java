package com.example.auth.inscription.adapters.out;

import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

@Component
public class VaultTransitAdapter implements UserKeyVaultPort 
{

    private final VaultTemplate vaultTemplate;

    public VaultTransitAdapter(VaultTemplate vaultTemplate) 
    {
        this.vaultTemplate = vaultTemplate;
    }

    @Override
    public void createSigningKey(String vaultKeyName) {
        // POST transit/keys/<name>  body: {"type":"ed25519"}
        vaultTemplate.write("transit/keys/" + vaultKeyName, Map.of("type", "ed25519"));
    }

    @Override
    public String exportPublicKey(String vaultKeyName) {
        // Lecture: transit/export/public-key/<name>
        // La structure exacte du JSON peut varier; ici on prend la forme la plus courante.
        VaultResponse resp = vaultTemplate.read("transit/export/public-key/" + vaultKeyName);
        if (resp == null || resp.getData() == null) {
            throw new IllegalStateException("Impossible d'exporter la clé publique depuis Vault pour: " + vaultKeyName);
        }

        // Souvent: data.keys."1" contient la clé (ou pem)
        Object keysObj = resp.getData().get("keys");
        if (!(keysObj instanceof Map<?, ?> keysMap)) {
            // fallback brut
            return resp.getData().toString();
        }

        Object v1 = keysMap.get("1");
        if (v1 == null) {
            // fallback brut
            return resp.getData().toString();
        }

        return v1.toString();
    }
}
