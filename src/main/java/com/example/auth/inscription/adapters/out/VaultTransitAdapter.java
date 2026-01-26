package com.example.auth.inscription.adapters.out;

import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Base64;
import java.util.Map;

@Component
public class VaultTransitAdapter implements UserKeyVaultPort {

    private final VaultTemplate vaultTemplate;

    public VaultTransitAdapter(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    @Override
    public void createSigningKey(String vaultKeyName) {
        vaultTemplate.write("transit/keys/" + vaultKeyName, Map.of("type", "ed25519"));
    }

    @Override
    public String exportPublicKey(String vaultKeyName) {
        VaultResponse resp = vaultTemplate.read("transit/export/public-key/" + vaultKeyName);
        if (resp == null || resp.getData() == null) {
            throw new IllegalStateException("Impossible d'exporter la clé publique depuis Vault pour: " + vaultKeyName);
        }
        Object keysObj = resp.getData().get("keys");
        if (!(keysObj instanceof Map<?, ?> keysMap)) {
            return resp.getData().toString();
        }
        Object v1 = keysMap.get("1");
        return v1 != null ? v1.toString() : resp.getData().toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> dataMap(Object data) {
        return data instanceof Map ? (Map<String, Object>) data : Map.of();
    }

    @Override
    public String encryptDek(String keyName, byte[] dekPlaintext) {
        String plaintextB64 = Base64.getEncoder().encodeToString(dekPlaintext);
        var resp = vaultTemplate.write("transit/encrypt/" + keyName, Map.of("plaintext", plaintextB64));
        if (resp == null || resp.getData() == null) throw new IllegalStateException("Vault encrypt DEK a échoué pour: " + keyName);
        Object ct = dataMap(resp.getData()).get("ciphertext");
        if (ct == null) throw new IllegalStateException("Pas de ciphertext dans la réponse Vault encrypt.");
        return ct.toString();
    }

    @Override
    public byte[] decryptDek(String keyName, String ciphertext) {
        var resp = vaultTemplate.write("transit/decrypt/" + keyName, Map.of("ciphertext", ciphertext));
        if (resp == null || resp.getData() == null) throw new IllegalStateException("Vault decrypt DEK a échoué pour: " + keyName);
        Object pt = dataMap(resp.getData()).get("plaintext");
        if (pt == null) throw new IllegalStateException("Pas de plaintext dans la réponse Vault decrypt.");
        return Base64.getDecoder().decode(pt.toString());
    }

    @Override
    public String sign(String vaultKeyName, String inputBase64) {
        // Ed25519 : prehashed doit être false, pas de suffixe /sha2-256 (réservé RSA, etc.)
        Map<String, Object> body = Map.of("input", inputBase64, "prehashed", false);
        var resp = vaultTemplate.write("transit/sign/" + vaultKeyName, body);
        if (resp == null || resp.getData() == null) throw new IllegalStateException("Vault sign a échoué pour: " + vaultKeyName);
        Object sig = dataMap(resp.getData()).get("signature");
        if (sig == null) throw new IllegalStateException("Pas de signature dans la réponse Vault sign.");
        return sig.toString();
    }

    @Override
    public boolean verify(String vaultKeyName, String inputBase64, String signature) {
        // Ed25519 : prehashed doit être false, pas de suffixe /sha2-256
        Map<String, Object> body = Map.of("input", inputBase64, "signature", signature, "prehashed", false);
        var resp = vaultTemplate.write("transit/verify/" + vaultKeyName, body);
        if (resp == null || resp.getData() == null) return false;
        Object valid = dataMap(resp.getData()).get("valid");
        return Boolean.TRUE.equals(valid);
    }
}
