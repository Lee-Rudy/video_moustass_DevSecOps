package com.example.auth.inscriptionTest.adapters.out;

import com.example.auth.inscription.adapters.out.VaultTransitAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VaultTransitAdapterTest {

    private VaultTemplate vaultTemplate;
    private VaultTransitAdapter adapter;

    @BeforeEach
    void setUp() {
        vaultTemplate = mock(VaultTemplate.class);
        adapter = new VaultTransitAdapter(vaultTemplate);
    }

    @Test
    void createSigningKey_shouldWriteToVaultWithEd25519Type() {
        String keyName = "test-signing-key";

        adapter.createSigningKey(keyName);

        verify(vaultTemplate).write(eq("transit/keys/" + keyName), eq(Map.of("type", "ed25519")));
    }

    @Test
    void createSigningKey_shouldCallVaultWithCorrectPath() {
        String keyName = "user-signing-abc123";

        adapter.createSigningKey(keyName);

        verify(vaultTemplate, times(1)).write(eq("transit/keys/user-signing-abc123"), any());
    }

    @Test
    void exportPublicKey_shouldReturnPublicKey_whenVaultReturnsValidData() {
        String keyName = "test-key";
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();
        Map<String, String> keys = new HashMap<>();
        keys.put("1", "PUBLIC_KEY_VALUE_123");
        data.put("keys", keys);

        when(vaultTemplate.read("transit/export/public-key/" + keyName)).thenReturn(response);
        when(response.getData()).thenReturn(data);

        String result = adapter.exportPublicKey(keyName);

        assertEquals("PUBLIC_KEY_VALUE_123", result);
        verify(vaultTemplate).read("transit/export/public-key/" + keyName);
    }

    @Test
    void exportPublicKey_shouldThrowException_whenVaultResponseIsNull() {
        String keyName = "missing-key";

        when(vaultTemplate.read(anyString())).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> adapter.exportPublicKey(keyName));
        assertTrue(ex.getMessage().contains("Impossible d'exporter la clé publique"));
        assertTrue(ex.getMessage().contains(keyName));
    }

    @Test
    void exportPublicKey_shouldThrowException_whenDataIsNull() {
        String keyName = "test-key";
        VaultResponse response = mock(VaultResponse.class);

        when(vaultTemplate.read(anyString())).thenReturn(response);
        when(response.getData()).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> adapter.exportPublicKey(keyName));
        assertTrue(ex.getMessage().contains("Impossible d'exporter"));
    }

    @Test
    void exportPublicKey_shouldReturnDataToString_whenKeysIsNotMap() {
        String keyName = "test-key";
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();
        data.put("keys", "NOT_A_MAP");

        when(vaultTemplate.read(anyString())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        String result = adapter.exportPublicKey(keyName);

        assertNotNull(result);
        assertTrue(result.contains("keys"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void encryptDek_shouldReturnCiphertext_whenVaultEncryptsSuccessfully() {
        String keyName = "video-dek";
        byte[] dekPlaintext = "MY_SECRET_DEK_32_BYTES_LONG_KEY!".getBytes();
        String expectedBase64 = Base64.getEncoder().encodeToString(dekPlaintext);

        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("ciphertext", "vault:v1:ENCRYPTED_VALUE");

        when(vaultTemplate.write(eq("transit/encrypt/" + keyName), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        String result = adapter.encryptDek(keyName, dekPlaintext);

        assertEquals("vault:v1:ENCRYPTED_VALUE", result);
        verify(vaultTemplate).write(eq("transit/encrypt/" + keyName), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("plaintext") && m.get("plaintext").equals(expectedBase64);
        }));
    }

    @Test
    void encryptDek_shouldThrowException_whenVaultResponseIsNull() {
        String keyName = "video-dek";
        byte[] dek = new byte[32];

        when(vaultTemplate.write(anyString(), any())).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> adapter.encryptDek(keyName, dek));
        assertTrue(ex.getMessage().contains("Vault encrypt DEK a échoué"));
    }

    @Test
    void encryptDek_shouldThrowException_whenDataIsNull() {
        String keyName = "video-dek";
        byte[] dek = new byte[32];
        VaultResponse response = mock(VaultResponse.class);

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> adapter.encryptDek(keyName, dek));
    }

    @Test
    void encryptDek_shouldThrowException_whenCiphertextIsMissing() {
        String keyName = "video-dek";
        byte[] dek = new byte[32];
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> adapter.encryptDek(keyName, dek));
        assertTrue(ex.getMessage().contains("Pas de ciphertext"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void decryptDek_shouldReturnDecryptedBytes_whenVaultDecryptsSuccessfully() {
        String keyName = "video-dek";
        String ciphertext = "vault:v1:ENCRYPTED";
        byte[] expectedPlaintext = "MY_SECRET_KEY".getBytes();
        String plaintextBase64 = Base64.getEncoder().encodeToString(expectedPlaintext);

        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("plaintext", plaintextBase64);

        when(vaultTemplate.write(eq("transit/decrypt/" + keyName), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        byte[] result = adapter.decryptDek(keyName, ciphertext);

        assertArrayEquals(expectedPlaintext, result);
        verify(vaultTemplate).write(eq("transit/decrypt/" + keyName), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("ciphertext") && m.get("ciphertext").equals(ciphertext);
        }));
    }

    @Test
    void decryptDek_shouldThrowException_whenVaultResponseIsNull() {
        when(vaultTemplate.write(anyString(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, 
            () -> adapter.decryptDek("key", "cipher"));
    }

    @Test
    void decryptDek_shouldThrowException_whenPlaintextIsMissing() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> adapter.decryptDek("key", "cipher"));
        assertTrue(ex.getMessage().contains("Pas de plaintext"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sign_shouldReturnSignature_whenVaultSignsSuccessfully() {
        String keyName = "user-signing-key";
        String inputBase64 = "BASE64_HASH";

        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("signature", "vault:v1:SIGNATURE_VALUE");

        when(vaultTemplate.write(eq("transit/sign/" + keyName), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        String result = adapter.sign(keyName, inputBase64);

        assertEquals("vault:v1:SIGNATURE_VALUE", result);
        verify(vaultTemplate).write(eq("transit/sign/" + keyName), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("input") && m.get("input").equals(inputBase64) &&
                   m.containsKey("prehashed") && m.get("prehashed").equals(false);
        }));
    }

    @Test
    void sign_shouldThrowException_whenVaultResponseIsNull() {
        when(vaultTemplate.write(anyString(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, 
            () -> adapter.sign("key", "data"));
    }

    @Test
    void sign_shouldThrowException_whenSignatureIsMissing() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> adapter.sign("key", "data"));
        assertTrue(ex.getMessage().contains("Pas de signature"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void verify_shouldReturnTrue_whenSignatureIsValid() {
        String keyName = "user-key";
        String inputBase64 = "HASH_BASE64";
        String signature = "vault:v1:SIG";

        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("valid", true);

        when(vaultTemplate.write(eq("transit/verify/" + keyName), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        boolean result = adapter.verify(keyName, inputBase64, signature);

        assertTrue(result);
        verify(vaultTemplate).write(eq("transit/verify/" + keyName), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("input") && m.get("input").equals(inputBase64) &&
                   m.containsKey("signature") && m.get("signature").equals(signature) &&
                   m.containsKey("prehashed") && m.get("prehashed").equals(false);
        }));
    }

    @Test
    void verify_shouldReturnFalse_whenSignatureIsInvalid() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("valid", false);

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        boolean result = adapter.verify("key", "input", "sig");

        assertFalse(result);
    }

    @Test
    void verify_shouldReturnFalse_whenVaultResponseIsNull() {
        when(vaultTemplate.write(anyString(), any())).thenReturn(null);

        boolean result = adapter.verify("key", "input", "sig");

        assertFalse(result);
    }

    @Test
    void verify_shouldReturnFalse_whenDataIsNull() {
        VaultResponse response = mock(VaultResponse.class);

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(null);

        boolean result = adapter.verify("key", "input", "sig");

        assertFalse(result);
    }

    @Test
    void verify_shouldReturnFalse_whenValidFieldIsMissing() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        boolean result = adapter.verify("key", "input", "sig");

        assertFalse(result);
    }

    @Test
    void exportPublicKey_shouldReturnVersion1Key_whenMultipleVersionsExist() {
        String keyName = "multi-version-key";
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = new HashMap<>();
        Map<String, String> keys = new HashMap<>();
        keys.put("1", "VERSION_1_KEY");
        keys.put("2", "VERSION_2_KEY");
        data.put("keys", keys);

        when(vaultTemplate.read(anyString())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        String result = adapter.exportPublicKey(keyName);

        assertEquals("VERSION_1_KEY", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void encryptDek_shouldEncodeInputAsBase64() {
        byte[] dek = "TEST_DEK_12345678901234567890123".getBytes();
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("ciphertext", "encrypted");

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        adapter.encryptDek("key", dek);

        String expectedBase64 = Base64.getEncoder().encodeToString(dek);
        verify(vaultTemplate).write(anyString(), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.get("plaintext").equals(expectedBase64);
        }));
    }

    @Test
    void decryptDek_shouldDecodeBase64Result() {
        String keyName = "decrypt-key";
        String ciphertext = "vault:v1:CIPHER";
        byte[] expectedBytes = "DECRYPTED_DATA".getBytes();
        String base64 = Base64.getEncoder().encodeToString(expectedBytes);

        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("plaintext", base64);

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        byte[] result = adapter.decryptDek(keyName, ciphertext);

        assertArrayEquals(expectedBytes, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sign_shouldSendPrehashedFalse() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("signature", "sig");

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        adapter.sign("key", "data");

        verify(vaultTemplate).write(anyString(), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("prehashed") && m.get("prehashed").equals(false);
        }));
    }

    @Test
    @SuppressWarnings("unchecked")
    void verify_shouldSendPrehashedFalse() {
        VaultResponse response = mock(VaultResponse.class);
        Map<String, Object> data = Map.of("valid", true);

        when(vaultTemplate.write(anyString(), any())).thenReturn(response);
        when(response.getData()).thenReturn(data);

        adapter.verify("key", "data", "sig");

        verify(vaultTemplate).write(anyString(), argThat(map -> {
            Map<String, Object> m = (Map<String, Object>) map;
            return m.containsKey("prehashed") && m.get("prehashed").equals(false);
        }));
    }
}
