package com.example.auth.orderTest;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import com.example.auth.login.entity.SignatureTransactionJpaEntity;
import com.example.auth.login.ports.SpringDataSignatureTransactionRepository;
import com.example.auth.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private SpringDataSignatureTransactionRepository sigRepo;
    private SpringDataUsersRepository userRepo;
    private UserKeyVaultPort vaultPort;
    private OrderService orderService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        sigRepo = mock(SpringDataSignatureTransactionRepository.class);
        userRepo = mock(SpringDataUsersRepository.class);
        vaultPort = mock(UserKeyVaultPort.class);
        orderService = new OrderService(sigRepo, userRepo, vaultPort);
        
        // Configurer les propriétés
        ReflectionTestUtils.setField(orderService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(orderService, "videoDekKeyName", "test-dek-key");
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {
        // Arrange
        Integer userId = 1;
        UsersJpaEntity user = createValidUser(userId, "Alice", false);
        MultipartFile videoFile = createMockVideoFile("video.mp4", "VIDEO_CONTENT".getBytes());

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any(byte[].class))).thenReturn("vault:v1:ENCRYPTED_DEK");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("vault:v1:SIGNATURE");
        when(sigRepo.save(any(SignatureTransactionJpaEntity.class)))
            .thenAnswer(inv -> {
                SignatureTransactionJpaEntity e = inv.getArgument(0);
                e.setId(100);
                return e;
            });

        // Act
        OrderService.CreateOrderResult result = orderService.createOrder(
            userId, "Bob", new BigDecimal("150.50"), "test-video.mp4", videoFile
        );

        // Assert
        assertNotNull(result);
        assertEquals(100, result.id());
        assertEquals(2, result.steps().size());
        assertTrue(result.steps().contains("Vidéo chiffrée"));
        assertTrue(result.steps().contains("Vidéo signée RSA"));

        verify(userRepo).findById(userId);
        verify(vaultPort).encryptDek(eq("test-dek-key"), any(byte[].class));
        verify(vaultPort).sign(eq("vault-key-alice"), anyString());
        verify(sigRepo).save(any(SignatureTransactionJpaEntity.class));
    }

    @Test
    void createOrder_shouldThrowException_whenUserNotFound() {
        when(userRepo.findById(999)).thenReturn(Optional.empty());
        MultipartFile video = createMockVideoFile("test.mp4", "data".getBytes());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.createOrder(999, "Bob", BigDecimal.TEN, "video.mp4", video)
        );
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(vaultPort, never()).encryptDek(anyString(), any());
    }

    @Test
    void createOrder_shouldThrowException_whenUserIsAdmin() {
        Integer adminId = 10;
        UsersJpaEntity admin = createValidUser(adminId, "Admin", true);

        when(userRepo.findById(adminId)).thenReturn(Optional.of(admin));
        MultipartFile video = createMockVideoFile("test.mp4", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(adminId, "User", BigDecimal.TEN, "video.mp4", video)
        );
        assertTrue(ex.getMessage().contains("administrateur ne peut pas créer d'ordre"));
        verify(vaultPort, never()).sign(anyString(), anyString());
    }

    @Test
    void createOrder_shouldThrowException_whenVaultKeyMissing() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        user.setAdmin(false);
        user.setVaultKey(null); // Clé manquante
        user.setPublicKey("PUB");

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        MultipartFile video = createMockVideoFile("test.mp4", "data".getBytes());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video)
        );
        assertTrue(ex.getMessage().contains("Clés Vault manquantes"));
    }

    @Test
    void createOrder_shouldThrowException_whenPublicKeyMissing() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        user.setAdmin(false);
        user.setVaultKey("vault-key");
        user.setPublicKey(null); // Clé publique manquante

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        MultipartFile video = createMockVideoFile("test.mp4", "data".getBytes());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video)
        );
        assertTrue(ex.getMessage().contains("Clés Vault manquantes"));
    }

    @Test
    void createOrder_shouldThrowException_whenVideoIsEmpty() {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile emptyVideo = createMockVideoFile("empty.mp4", new byte[0]);

        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", emptyVideo)
        );
        assertTrue(ex.getMessage().contains("Fichier vidéo vide"));
    }

    @Test
    void createOrder_shouldSaveVideoOnDisk() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        byte[] videoData = "MY_VIDEO_DATA".getBytes();
        MultipartFile video = createMockVideoFile("test.mp4", videoData);

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any(byte[].class))).thenReturn("encrypted_dek");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("signature");
        when(sigRepo.save(any())).thenAnswer(inv -> {
            SignatureTransactionJpaEntity e = inv.getArgument(0);
            e.setId(50);
            return e;
        });

        orderService.createOrder(1, "Bob", BigDecimal.valueOf(100), "video.mp4", video);

        // Vérifier que des fichiers ont été créés dans tempDir
        assertTrue(Files.list(tempDir).count() >= 2); // .enc et .dek
    }

    @Test
    void createOrder_shouldSaveTransactionWithCorrectFields() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile video = createMockVideoFile("test.mp4", "DATA".getBytes());

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("enc_dek");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(1, "Bob", new BigDecimal("250.75"), "my-video.mp4", video);

        ArgumentCaptor<SignatureTransactionJpaEntity> captor = ArgumentCaptor.forClass(SignatureTransactionJpaEntity.class);
        verify(sigRepo).save(captor.capture());

        SignatureTransactionJpaEntity saved = captor.getValue();
        assertEquals(1, saved.getUserId());
        assertEquals("Bob", saved.getTransactionSendTo());
        assertEquals("my-video.mp4", saved.getVideoName());
        assertEquals(new BigDecimal("250.75"), saved.getMontantTransaction());
        assertTrue(saved.isActive());
        assertNotNull(saved.getVideoHash());
        assertNotNull(saved.getSignature());
        assertNotNull(saved.getPublicKey());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getOrdersReceived_shouldReturnOrdersForUser() {
        UsersJpaEntity user = createValidUser(5, "Bob", false);
        SignatureTransactionJpaEntity order1 = new SignatureTransactionJpaEntity();
        order1.setId(1);
        order1.setTransactionSendTo("Bob");

        SignatureTransactionJpaEntity order2 = new SignatureTransactionJpaEntity();
        order2.setId(2);
        order2.setTransactionSendTo("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(sigRepo.findByTransactionSendToOrderByCreatedAtDesc("Bob"))
            .thenReturn(Arrays.asList(order1, order2));

        List<SignatureTransactionJpaEntity> result = orderService.getOrdersReceived(5);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(sigRepo).findByTransactionSendToOrderByCreatedAtDesc("Bob");
    }

    @Test
    void getOrdersReceived_shouldThrowException_whenUserNotFound() {
        when(userRepo.findById(999)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.getOrdersReceived(999)
        );
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
    }

    @Test
    void getOrdersReceived_shouldHandleUserWithNullName() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName(null);
        user.setAdmin(false);

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(sigRepo.findByTransactionSendToOrderByCreatedAtDesc(""))
            .thenReturn(List.of());

        List<SignatureTransactionJpaEntity> result = orderService.getOrdersReceived(5);

        assertTrue(result.isEmpty());
        verify(sigRepo).findByTransactionSendToOrderByCreatedAtDesc("");
    }

    @Test
    void validateOrder_shouldThrowException_whenOrderNotFound() {
        when(sigRepo.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            orderService.validateOrder(999, "Bob")
        );
        assertTrue(ex.getMessage().contains("Ordre introuvable"));
    }

    @Test
    void validateOrder_shouldThrowException_whenOrderNotForCurrentUser() {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setTransactionSendTo("Alice");

        when(sigRepo.findById(1)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("ne vous est pas destiné"));
    }

    @Test
    void validateOrder_shouldSucceed_whenAllConditionsMet() throws Exception {
        // Setup: Create a valid order with encrypted video
        UsersJpaEntity sender = createValidUser(1, "Alice", false);
        UsersJpaEntity receiver = createValidUser(2, "Bob", false);
        
        byte[] originalVideo = "ORIGINAL_VIDEO_CONTENT".getBytes();
        
        // Encrypt video using AES-GCM
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[32];
        rng.nextBytes(dek);
        byte[] iv = new byte[12];
        rng.nextBytes(iv);
        
        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(originalVideo);
        
        byte[] fileContent = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, fileContent, 0, iv.length);
        System.arraycopy(encrypted, 0, fileContent, iv.length, encrypted.length);
        
        // Create files
        Path videoPath = tempDir.resolve("test_video.enc");
        Path dekPath = tempDir.resolve("test_video.enc.dek");
        Files.write(videoPath, fileContent);
        
        String encryptedDek = "vault:v1:ENCRYPTED_DEK";
        Files.writeString(dekPath, encryptedDek);
        
        // Calculate hash
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(originalVideo);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        
        String signature = "vault:v1:SIGNATURE";
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(100);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        order.setPathVideo(videoPath.toString());
        order.setSignature(signature);
        
        when(sigRepo.findById(100)).thenReturn(Optional.of(order));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(vaultPort.decryptDek(anyString(), eq(encryptedDek))).thenReturn(dek);
        when(vaultPort.verify(eq("vault-key-alice"), eq(hashBase64), eq(signature))).thenReturn(true);
        
        OrderService.ValidateOrderResult result = orderService.validateOrder(100, "Bob");
        
        assertTrue(result.success());
        assertNotNull(result.videoBase64());
        
        // Decode and verify video content
        byte[] decodedVideo = Base64.getDecoder().decode(result.videoBase64());
        assertArrayEquals(originalVideo, decodedVideo);
    }

    @Test
    void validateOrder_shouldThrowException_whenVideoFileNotFound() {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        order.setPathVideo(tempDir.resolve("nonexistent.enc").toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Fichier vidéo introuvable"));
    }

    @Test
    void validateOrder_shouldThrowException_whenVideoFileEmpty() throws Exception {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        
        Path videoPath = tempDir.resolve("empty.enc");
        Files.write(videoPath, new byte[0]);
        order.setPathVideo(videoPath.toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Fichier vidéo introuvable ou vide"));
    }

    @Test
    void validateOrder_shouldThrowException_whenDekFileNotFound() throws Exception {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        
        Path videoPath = tempDir.resolve("video.enc");
        Files.write(videoPath, "some data".getBytes());
        order.setPathVideo(videoPath.toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Fichier DEK introuvable"));
    }

    @Test
    void validateOrder_shouldThrowException_whenDekFileEmpty() throws Exception {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        
        Path videoPath = tempDir.resolve("video.enc");
        Path dekPath = tempDir.resolve("video.enc.dek");
        Files.write(videoPath, "some data".getBytes());
        Files.writeString(dekPath, "");
        order.setPathVideo(videoPath.toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Fichier DEK introuvable ou vide"));
    }

    @Test
    void validateOrder_shouldThrowException_whenVideoCorrupted() throws Exception {
        UsersJpaEntity sender = createValidUser(1, "Alice", false);
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        
        Path videoPath = tempDir.resolve("corrupted.enc");
        Path dekPath = tempDir.resolve("corrupted.enc.dek");
        
        // Write too short data (less than IV length)
        Files.write(videoPath, new byte[]{1, 2, 3});
        Files.writeString(dekPath, "vault:v1:ENCRYPTED_DEK");
        
        order.setPathVideo(videoPath.toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(vaultPort.decryptDek(anyString(), anyString())).thenReturn(new byte[32]);
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Fichier vidéo corrompu"));
    }

    @Test
    void validateOrder_shouldThrowException_whenSignatureInvalid() throws Exception {
        UsersJpaEntity sender = createValidUser(1, "Alice", false);
        
        byte[] originalVideo = "VIDEO_DATA".getBytes();
        
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[32];
        rng.nextBytes(dek);
        byte[] iv = new byte[12];
        rng.nextBytes(iv);
        
        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(originalVideo);
        
        byte[] fileContent = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, fileContent, 0, iv.length);
        System.arraycopy(encrypted, 0, fileContent, iv.length, encrypted.length);
        
        Path videoPath = tempDir.resolve("video.enc");
        Path dekPath = tempDir.resolve("video.enc.dek");
        Files.write(videoPath, fileContent);
        Files.writeString(dekPath, "vault:v1:ENCRYPTED_DEK");
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        order.setPathVideo(videoPath.toString());
        order.setSignature("vault:v1:INVALID_SIGNATURE");
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(vaultPort.decryptDek(anyString(), anyString())).thenReturn(dek);
        when(vaultPort.verify(anyString(), anyString(), anyString())).thenReturn(false);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Vidéo corrompue"));
    }

    @Test
    void validateOrder_shouldThrowException_whenSenderVaultKeyMissing() throws Exception {
        UsersJpaEntity sender = new UsersJpaEntity();
        sender.setId(1);
        sender.setName("Alice");
        sender.setVaultKey(null); // Missing vault key
        
        byte[] originalVideo = "VIDEO_DATA".getBytes();
        
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[32];
        rng.nextBytes(dek);
        byte[] iv = new byte[12];
        rng.nextBytes(iv);
        
        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(originalVideo);
        
        byte[] fileContent = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, fileContent, 0, iv.length);
        System.arraycopy(encrypted, 0, fileContent, iv.length, encrypted.length);
        
        Path videoPath = tempDir.resolve("video.enc");
        Path dekPath = tempDir.resolve("video.enc.dek");
        Files.write(videoPath, fileContent);
        Files.writeString(dekPath, "vault:v1:ENCRYPTED_DEK");
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("Bob");
        order.setPathVideo(videoPath.toString());
        order.setSignature("vault:v1:SIG");
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(vaultPort.decryptDek(anyString(), anyString())).thenReturn(dek);
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("clé de signature de l'expéditeur indisponible"));
    }

    @Test
    void validateOrder_shouldThrowException_whenSenderNotFound() throws Exception {
        byte[] originalVideo = "VIDEO_DATA".getBytes();
        
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[32];
        rng.nextBytes(dek);
        byte[] iv = new byte[12];
        rng.nextBytes(iv);
        
        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(originalVideo);
        
        byte[] fileContent = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, fileContent, 0, iv.length);
        System.arraycopy(encrypted, 0, fileContent, iv.length, encrypted.length);
        
        Path videoPath = tempDir.resolve("video.enc");
        Path dekPath = tempDir.resolve("video.enc.dek");
        Files.write(videoPath, fileContent);
        Files.writeString(dekPath, "vault:v1:ENCRYPTED_DEK");
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(999); // Non-existent user
        order.setTransactionSendTo("Bob");
        order.setPathVideo(videoPath.toString());
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        when(userRepo.findById(999)).thenReturn(Optional.empty());
        when(vaultPort.decryptDek(anyString(), anyString())).thenReturn(dek);
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            orderService.validateOrder(1, "Bob")
        );
        assertTrue(ex.getMessage().contains("Expéditeur introuvable"));
    }

    @Test
    void validateOrder_shouldHandleWhitespaceInTransactionSendTo() throws Exception {
        UsersJpaEntity sender = createValidUser(1, "Alice", false);
        
        byte[] originalVideo = "VIDEO_DATA".getBytes();
        
        SecureRandom rng = new SecureRandom();
        byte[] dek = new byte[32];
        rng.nextBytes(dek);
        byte[] iv = new byte[12];
        rng.nextBytes(iv);
        
        SecretKey key = new SecretKeySpec(dek, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(originalVideo);
        
        byte[] fileContent = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, fileContent, 0, iv.length);
        System.arraycopy(encrypted, 0, fileContent, iv.length, encrypted.length);
        
        Path videoPath = tempDir.resolve("video.enc");
        Path dekPath = tempDir.resolve("video.enc.dek");
        Files.write(videoPath, fileContent);
        Files.writeString(dekPath, "vault:v1:ENCRYPTED_DEK");
        
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(1);
        order.setUserId(1);
        order.setTransactionSendTo("  Bob  ");
        order.setPathVideo(videoPath.toString());
        order.setSignature("vault:v1:SIG");
        
        when(sigRepo.findById(1)).thenReturn(Optional.of(order));
        when(userRepo.findById(1)).thenReturn(Optional.of(sender));
        when(vaultPort.decryptDek(anyString(), anyString())).thenReturn(dek);
        when(vaultPort.verify(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Should work because both are trimmed
        assertDoesNotThrow(() -> orderService.validateOrder(1, "  Bob  "));
    }

    @Test
    void createOrder_shouldHashVideoBeforeEncryption() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        byte[] videoData = "VIDEO_TO_HASH".getBytes();
        MultipartFile video = createMockVideoFile("test.mp4", videoData);

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("enc_dek");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video);

        ArgumentCaptor<SignatureTransactionJpaEntity> captor = ArgumentCaptor.forClass(SignatureTransactionJpaEntity.class);
        verify(sigRepo).save(captor.capture());

        String hash = captor.getValue().getVideoHash();
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        // SHA-256 hash est de 64 caractères en hexadécimal
        assertEquals(64, hash.length());
    }

    @Test
    void createOrder_shouldCallVaultSignWithHashBase64() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile video = createMockVideoFile("test.mp4", "DATA".getBytes());

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("enc");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video);

        // Vérifier que sign est appelé avec un Base64
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(vaultPort).sign(eq("vault-key-alice"), hashCaptor.capture());
        
        String hashBase64 = hashCaptor.getValue();
        assertNotNull(hashBase64);
        // Vérifier que c'est du Base64 valide
        assertDoesNotThrow(() -> Base64.getDecoder().decode(hashBase64));
    }

    @Test
    void getOrdersReceived_shouldReturnEmptyList_whenNoOrders() {
        UsersJpaEntity user = createValidUser(5, "Bob", false);

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(sigRepo.findByTransactionSendToOrderByCreatedAtDesc("Bob"))
            .thenReturn(List.of());

        List<SignatureTransactionJpaEntity> result = orderService.getOrdersReceived(5);

        assertTrue(result.isEmpty());
    }

    @Test
    void createOrder_shouldCreateDekFile() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile video = createMockVideoFile("test.mp4", "DATA".getBytes());

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("ENCRYPTED_DEK_DATA");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> {
            SignatureTransactionJpaEntity e = inv.getArgument(0);
            e.setId(10);
            return e;
        });

        orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video);

        // Vérifier qu'un fichier .dek a été créé
        long dekFileCount = Files.walk(tempDir)
            .filter(p -> p.toString().endsWith(".dek"))
            .count();
        assertEquals(1, dekFileCount);
    }

    @Test
    void createOrder_shouldSetExpiredVideoTo2HoursFromNow() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile video = createMockVideoFile("test.mp4", "DATA".getBytes());

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("enc");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video);

        ArgumentCaptor<SignatureTransactionJpaEntity> captor = ArgumentCaptor.forClass(SignatureTransactionJpaEntity.class);
        verify(sigRepo).save(captor.capture());

        SignatureTransactionJpaEntity saved = captor.getValue();
        assertNotNull(saved.getExpiredVideo());
        assertNotNull(saved.getCreatedAt());
        // L'expiration devrait être environ 2h après la création
        assertTrue(saved.getExpiredVideo().isAfter(saved.getCreatedAt()));
    }

    @Test
    void createOrder_shouldSetActiveToTrue() throws Exception {
        UsersJpaEntity user = createValidUser(1, "Alice", false);
        MultipartFile video = createMockVideoFile("test.mp4", "DATA".getBytes());

        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(vaultPort.encryptDek(anyString(), any())).thenReturn("enc");
        when(vaultPort.sign(anyString(), anyString())).thenReturn("sig");
        when(sigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.createOrder(1, "Bob", BigDecimal.TEN, "video.mp4", video);

        ArgumentCaptor<SignatureTransactionJpaEntity> captor = ArgumentCaptor.forClass(SignatureTransactionJpaEntity.class);
        verify(sigRepo).save(captor.capture());

        assertTrue(captor.getValue().isActive());
    }

    // Helper methods
    private UsersJpaEntity createValidUser(Integer id, String name, boolean isAdmin) {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(id);
        user.setName(name);
        user.setMail(name.toLowerCase() + "@test.com");
        user.setAdmin(isAdmin);
        user.setVaultKey("vault-key-" + name.toLowerCase());
        user.setPublicKey("PUBLIC_KEY_" + name.toUpperCase());
        return user;
    }

    private MultipartFile createMockVideoFile(String filename, byte[] content) {
        MultipartFile mock = mock(MultipartFile.class);
        try {
            when(mock.getBytes()).thenReturn(content);
            when(mock.isEmpty()).thenReturn(content.length == 0);
            when(mock.getOriginalFilename()).thenReturn(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mock;
    }
}
