package com.example.auth.orderTest;

import com.example.auth.audit.service.AuditLogService;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.login.entity.SignatureTransactionJpaEntity;
import com.example.auth.order.OrderController;
import com.example.auth.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService orderService;

    @MockBean
    SpringDataUsersRepository userRepo;

    @MockBean
    AuditLogService auditLogService;

    @Test
    void create_shouldReturnOk_whenOrderCreatedSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "VIDEO_DATA".getBytes());
        
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(orderService.createOrder(eq(1), eq("Bob"), any(BigDecimal.class), eq("test-video.mp4"), any()))
            .thenReturn(new OrderService.CreateOrderResult(100, List.of("Vidéo chiffrée", "Vidéo signée RSA")));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "150.50")
                        .param("video_name", "test-video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[0]").value("Vidéo chiffrée"));

        verify(orderService).createOrder(eq(1), eq("Bob"), eq(new BigDecimal("150.50")), eq("test-video.mp4"), any());
        verify(auditLogService).logAction(anyInt(), eq("TX_CREATED"), eq("signature_transactions"), eq(100), anyString(), any());
    }

    @Test
    void create_shouldReturn400_whenTransactionSendToMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("transaction_send_to requis"));

        verify(orderService, never()).createOrder(anyInt(), anyString(), any(), anyString(), any());
    }

    @Test
    void create_shouldReturn400_whenTransactionSendToIsBlank() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "   ")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("transaction_send_to requis"));
    }

    @Test
    void create_shouldReturn400_whenVideoNameMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("video_name requis"));
    }

    @Test
    void create_shouldReturn400_whenVideoFileMissing() throws Exception {
        mockMvc.perform(multipart("/api/orders")
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fichier vidéo requis"));
    }

    @Test
    void create_shouldReturn400_whenMontantInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "NOT_A_NUMBER")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Montant invalide"));
    }

    @Test
    void create_shouldReturn400_whenServiceThrowsIllegalArgumentException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenThrow(new IllegalArgumentException("Admin cannot create order"));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Admin cannot create order"));
    }

    @Test
    void create_shouldReturn500_whenServiceThrowsIllegalStateException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenThrow(new IllegalStateException("Vault keys missing"));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Vault keys missing"));
    }

    @Test
    void create_shouldReturn500_whenGeneralSecurityException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenThrow(new java.security.GeneralSecurityException("Encryption failed"));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Erreur de chiffrement ou signature"));
    }

    @Test
    void received_shouldReturnOrdersList() throws Exception {
        SignatureTransactionJpaEntity order1 = createOrder(1, "video1.mp4", "hash1");
        SignatureTransactionJpaEntity order2 = createOrder(2, "video2.mp4", "hash2");

        when(orderService.getOrdersReceived(5)).thenReturn(Arrays.asList(order1, order2));

        mockMvc.perform(get("/api/orders/received")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].videoName").value("video1.mp4"))
                .andExpect(jsonPath("$[0].videoHash").value("hash1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].videoName").value("video2.mp4"));

        verify(orderService).getOrdersReceived(5);
    }

    @Test
    void received_shouldReturnEmptyList_whenNoOrders() throws Exception {
        when(orderService.getOrdersReceived(10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/received")
                        .requestAttr("userId", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService).getOrdersReceived(10);
    }

    @Test
    void validate_shouldReturnVideoBase64_whenValidationSuccessful() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(eq(123), eq("Bob")))
            .thenReturn(new OrderService.ValidateOrderResult(true, "BASE64_VIDEO_DATA"));

        mockMvc.perform(post("/api/orders/123/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.videoBase64").value("BASE64_VIDEO_DATA"));

        verify(orderService).validateOrder(123, "Bob");
        verify(auditLogService).logAction(eq(5), eq("TX_VALIDATED"), eq("signature_transactions"), eq(123), anyString(), any());
    }

    @Test
    void validate_shouldReturn400_whenOrderNotForUser() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(eq(123), eq("Bob")))
            .thenThrow(new IllegalArgumentException("Cet ordre ne vous est pas destiné."));

        mockMvc.perform(post("/api/orders/123/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cet ordre ne vous est pas destiné."));

        verify(auditLogService, never()).logAction(anyInt(), anyString(), anyString(), anyInt(), anyString(), any());
    }

    @Test
    void validate_shouldReturn400_whenVideoCorrupted() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(anyInt(), anyString()))
            .thenThrow(new IllegalStateException("Fichier vidéo introuvable ou vide."));

        mockMvc.perform(post("/api/orders/10/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Fichier vidéo introuvable ou vide."));
    }

    @Test
    void validate_shouldReturn500_whenUnexpectedException() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(anyInt(), anyString()))
            .thenThrow(new IOException("Disk error"));

        mockMvc.perform(post("/api/orders/10/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Disk error"));
    }

    @Test
    void create_shouldTrimParameters() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(orderService.createOrder(eq(1), eq("Bob"), any(), eq("video.mp4"), any()))
            .thenReturn(new OrderService.CreateOrderResult(10, List.of("Done")));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "  Bob  ")
                        .param("montant", "  100.00  ")
                        .param("video_name", "  video.mp4  ")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk());

        verify(orderService).createOrder(eq(1), eq("Bob"), eq(new BigDecimal("100.00")), eq("video.mp4"), any());
    }

    @Test
    void create_shouldLogAction_whenOrderCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenReturn(new OrderService.CreateOrderResult(50, List.of("Done")));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "99.99")
                        .param("video_name", "my-video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk());

        verify(auditLogService).logAction(
            eq(1),
            eq("TX_CREATED"),
            eq("signature_transactions"),
            eq(50),
            argThat(msg -> msg.contains("Alice") && msg.contains("Bob") && msg.contains("my-video.mp4")),
            any()
        );
    }

    @Test
    void received_shouldMapAllFieldsCorrectly() throws Exception {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(123);
        order.setVideoName("test.mp4");
        order.setVideoHash("abc123hash");
        order.setPathVideo("/path/to/video.enc");
        order.setExpiredVideo(LocalDateTime.of(2026, 2, 1, 10, 0));
        order.setActive(true);
        order.setSignedAt(LocalDateTime.of(2026, 1, 27, 12, 0));
        order.setCreatedAt(LocalDateTime.of(2026, 1, 27, 11, 0));

        when(orderService.getOrdersReceived(5)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/received")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(123))
                .andExpect(jsonPath("$[0].videoName").value("test.mp4"))
                .andExpect(jsonPath("$[0].videoHash").value("abc123hash"))
                .andExpect(jsonPath("$[0].pathVideo").value("/path/to/video.enc"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void validate_shouldHandleUserWithNullName() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName(null);

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(eq(10), eq("")))
            .thenReturn(new OrderService.ValidateOrderResult(true, "VIDEO"));

        mockMvc.perform(post("/api/orders/10/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk());

        verify(orderService).validateOrder(10, "");
    }

    @Test
    void create_shouldHandleUserWithNullName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName(null);
        
        when(userRepo.findById(1)).thenReturn(Optional.of(user));
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenReturn(new OrderService.CreateOrderResult(10, List.of("Done")));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk());

        verify(auditLogService).logAction(
            eq(1),
            anyString(),
            anyString(),
            anyInt(),
            argThat(msg -> msg.contains("Utilisateur #1")),
            any()
        );
    }

    @Test
    void create_shouldReturn500_whenGenericException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "DATA".getBytes());
        
        when(orderService.createOrder(anyInt(), anyString(), any(), anyString(), any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(multipart("/api/orders")
                        .file(file)
                        .param("transaction_send_to", "Bob")
                        .param("montant", "100")
                        .param("video_name", "video.mp4")
                        .requestAttr("userId", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Unexpected error"));
    }

    @Test
    void validate_shouldLogValidationAction() throws Exception {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("Bob");

        when(userRepo.findById(5)).thenReturn(Optional.of(user));
        when(orderService.validateOrder(eq(123), eq("Bob")))
            .thenReturn(new OrderService.ValidateOrderResult(true, "VIDEO"));

        mockMvc.perform(post("/api/orders/123/validate")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk());

        verify(auditLogService).logAction(
            eq(5),
            eq("TX_VALIDATED"),
            eq("signature_transactions"),
            eq(123),
            argThat(msg -> msg.contains("Bob") && msg.contains("#123")),
            any()
        );
    }

    // Helper method
    private SignatureTransactionJpaEntity createOrder(int id, String videoName, String hash) {
        SignatureTransactionJpaEntity order = new SignatureTransactionJpaEntity();
        order.setId(id);
        order.setVideoName(videoName);
        order.setVideoHash(hash);
        order.setPathVideo("/path/" + videoName);
        order.setActive(true);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
