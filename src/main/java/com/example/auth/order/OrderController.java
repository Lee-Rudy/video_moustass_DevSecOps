package com.example.auth.order;

import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.login.entity.SignatureTransactionJpaEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final SpringDataUsersRepository userRepo;

    public OrderController(OrderService orderService, SpringDataUsersRepository userRepo) {
        this.orderService = orderService;
        this.userRepo = userRepo;
    }

    /**
     * POST /api/orders (multipart) : transaction_send_to, montant, video_name, video (fichier).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestAttribute("userId") Integer userId,
            @RequestParam("transaction_send_to") String transactionSendTo,
            @RequestParam("montant") String montantStr,
            @RequestParam("video_name") String videoName,
            @RequestParam("video") MultipartFile video) {

        if (transactionSendTo == null || transactionSendTo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "transaction_send_to requis"));
        }
        if (videoName == null || videoName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "video_name requis"));
        }
        if (video == null || video.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier vidéo requis"));
        }

        BigDecimal montant;
        try {
            montant = new BigDecimal(montantStr.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Montant invalide"));
        }

        try {
            OrderService.CreateOrderResult r = orderService.createOrder(userId, transactionSendTo.trim(), montant, videoName.trim(), video);
            return ResponseEntity.ok(Map.of("id", r.id(), "steps", r.steps()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur de chiffrement ou signature"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erreur serveur"));
        }
    }

    /**
     * GET /api/orders/received : ordres reçus par l'utilisateur connecté.
     */
    @GetMapping("/received")
    public ResponseEntity<List<OrderReceivedDto>> received(@RequestAttribute("userId") Integer userId) {
        List<SignatureTransactionJpaEntity> list = orderService.getOrdersReceived(userId);
        List<OrderReceivedDto> dtos = list.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/orders/:id/validate : scan, déchiffrement, vérification signature. Retourne videoBase64 ou erreur "Vidéo corrompue".
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validate(@RequestAttribute("userId") Integer userId, @PathVariable("id") Integer id) {
        String currentUserName = userRepo.findById(userId).map(u -> u.getName() != null ? u.getName() : "").orElse("");
        try {
            OrderService.ValidateOrderResult r = orderService.validateOrder(id, currentUserName);
            return ResponseEntity.ok(Map.of("success", r.success(), "videoBase64", r.videoBase64()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Erreur serveur"));
        }
    }

    private OrderReceivedDto toDto(SignatureTransactionJpaEntity e) {
        return new OrderReceivedDto(
                e.getId(),
                e.getVideoName(),
                e.getVideoHash(),
                e.getPathVideo(),
                e.getExpiredVideo() != null ? e.getExpiredVideo().toString() : null,
                e.isActive(),
                e.getSignedAt() != null ? e.getSignedAt().toString() : null,
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : null
        );
    }

    public record OrderReceivedDto(Integer id, String videoName, String videoHash, String pathVideo,
                                   String expiredVideo, boolean active, String signedAt, String createdAt) {}
}
