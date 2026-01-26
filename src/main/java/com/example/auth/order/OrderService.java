package com.example.auth.order;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.login.entity.SignatureTransactionJpaEntity;
import com.example.auth.login.ports.SpringDataSignatureTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int DEK_LENGTH = 32;

    private final SpringDataSignatureTransactionRepository sigRepo;
    private final SpringDataUsersRepository userRepo;
    private final UserKeyVaultPort vaultPort;

    @Value("${app.video.storage-path:./data/videos}")
    private String storagePath;

    @Value("${app.vault.video-dek-key:video-dek}")
    private String videoDekKeyName;

    public OrderService(SpringDataSignatureTransactionRepository sigRepo,
                        SpringDataUsersRepository userRepo,
                        UserKeyVaultPort vaultPort) {
        this.sigRepo = sigRepo;
        this.userRepo = userRepo;
        this.vaultPort = vaultPort;
    }

    /**
     * Crée un ordre : chiffre la vidéo, signe le hash, enregistre sur disque et en BDD.
     * Refuse si l'utilisateur est admin.
     */
    public CreateOrderResult createOrder(Integer userId, String transactionSendTo, BigDecimal montant,
                                         String videoName, MultipartFile video) throws IOException, GeneralSecurityException {
        UsersJpaEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
        if (user.isAdmin()) {
            throw new IllegalArgumentException("Un administrateur ne peut pas créer d'ordre de transaction.");
        }
        String vaultKey = user.getVaultKey();
        String publicKey = user.getPublicKey();
        if (vaultKey == null || vaultKey.isBlank() || publicKey == null || publicKey.isBlank()) {
            throw new IllegalStateException("Clés Vault manquantes pour cet utilisateur (vault_key, public_key).");
        }

        byte[] videoBytes = video.getBytes();
        if (videoBytes.length == 0) {
            throw new IllegalArgumentException("Fichier vidéo vide.");
        }

        // 1) Hash SHA-256 de la vidéo (avant chiffrement)
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(videoBytes);
        String videoHash = bytesToHex(hash);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        // 2) Chiffrement : DEK + AES-GCM, puis chiffrement du DEK par Vault
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[DEK_LENGTH];
        rng.nextBytes(dek);
        byte[] iv = new byte[GCM_IV_LENGTH];
        rng.nextBytes(iv);

        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(videoBytes);
        // Fichier : IV (12) || ciphertext (inclut le tag)
        byte[] toWrite = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, toWrite, 0, iv.length);
        System.arraycopy(encrypted, 0, toWrite, iv.length, encrypted.length);

        String encryptedDek = vaultPort.encryptDek(videoDekKeyName, dek);

        // 3) Signature du hash avec la clé privée de l'utilisateur (Vault Transit)
        String signature = vaultPort.sign(vaultKey, hashBase64);

        // 4) Sauvegarde sur disque
        Path root = Paths.get(storagePath).toAbsolutePath();
        Files.createDirectories(root);
        String baseName = userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "");
        String encFileName = baseName + ".enc";
        Path encPath = root.resolve(encFileName);
        Path dekPath = root.resolve(encFileName + ".dek");
        Files.write(encPath, toWrite);
        Files.writeString(dekPath, encryptedDek);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.plusHours(2);

        SignatureTransactionJpaEntity e = new SignatureTransactionJpaEntity();
        e.setUserId(userId);
        e.setTransactionSendTo(transactionSendTo);
        e.setVideoName(videoName);
        e.setMontantTransaction(montant);
        e.setVideoHash(videoHash);
        e.setPathVideo(encPath.toString());
        e.setExpiredVideo(expired);
        e.setActive(true);
        e.setPublicKey(publicKey);
        e.setSignature(signature);
        e.setSignedAt(now);
        e.setCreatedAt(now);
        sigRepo.save(e);

        return new CreateOrderResult(e.getId(), List.of("Vidéo chiffrée", "Vidéo signée RSA"));
    }

    /**
     * Ordres reçus par l'utilisateur (transaction_send_to = son nom).
     */
    public List<SignatureTransactionJpaEntity> getOrdersReceived(Integer userId) {
        UsersJpaEntity user = userRepo.findById(userId).orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
        String name = user.getName() != null ? user.getName().trim() : "";
        return sigRepo.findByTransactionSendToOrderByCreatedAtDesc(name);
    }

    /**
     * Valide un ordre : scan, déchiffrement, vérification de la signature. Retourne la vidéo en base64 ou lève en cas d'erreur.
     */
    public ValidateOrderResult validateOrder(Integer orderId, String currentUserName) throws IOException, GeneralSecurityException {
        SignatureTransactionJpaEntity order = sigRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre introuvable"));
        if (!currentUserName.trim().equals(order.getTransactionSendTo() != null ? order.getTransactionSendTo().trim() : "")) {
            throw new IllegalArgumentException("Cet ordre ne vous est pas destiné.");
        }

        Path encPath = Paths.get(order.getPathVideo());
        Path dekPath = Paths.get(order.getPathVideo() + ".dek");

        // 1) Scan : existence et non vide
        if (!Files.exists(encPath) || Files.size(encPath) == 0) {
            throw new IllegalStateException("Fichier vidéo introuvable ou vide.");
        }
        if (!Files.exists(dekPath) || Files.size(dekPath) == 0) {
            throw new IllegalStateException("Fichier DEK introuvable ou vide.");
        }

        // 2) Déchiffrement du DEK
        String encryptedDek = Files.readString(dekPath);
        byte[] dek = vaultPort.decryptDek(videoDekKeyName, encryptedDek);

        // 3) Déchiffrement de la vidéo (IV 12 + ciphertext)
        byte[] raw = Files.readAllBytes(encPath);
        if (raw.length <= GCM_IV_LENGTH) {
            throw new IllegalStateException("Fichier vidéo corrompu (taille).");
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(raw, 0, iv, 0, GCM_IV_LENGTH);
        byte[] ciphertext = new byte[raw.length - GCM_IV_LENGTH];
        System.arraycopy(raw, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] decrypted = cipher.doFinal(ciphertext);

        // 4) Vérification de la signature avec la clé de l'expéditeur (user_id)
        UsersJpaEntity sender = userRepo.findById(order.getUserId()).orElseThrow(() -> new IllegalStateException("Expéditeur introuvable"));
        String senderVaultKey = sender.getVaultKey();
        if (senderVaultKey == null || senderVaultKey.isBlank()) {
            throw new IllegalStateException("Vidéo corrompue : clé de signature de l'expéditeur indisponible.");
        }

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(decrypted);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        if (!vaultPort.verify(senderVaultKey, hashBase64, order.getSignature())) {
            throw new IllegalArgumentException("Vidéo corrompue.");
        }

        String videoBase64 = Base64.getEncoder().encodeToString(decrypted);
        return new ValidateOrderResult(true, videoBase64);
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    public record CreateOrderResult(int id, List<String> steps) {}

    public record ValidateOrderResult(boolean success, String videoBase64) {}
}
