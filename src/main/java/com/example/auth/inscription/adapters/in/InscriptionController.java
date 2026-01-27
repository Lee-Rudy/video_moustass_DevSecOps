package com.example.auth.inscription.adapters.in;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscription")
public class InscriptionController {

    private final InscriptionUseCase inscriptionUseCase;

    public InscriptionController(InscriptionUseCase inscriptionUseCase) {
        this.inscriptionUseCase = inscriptionUseCase;
    }

    /**
     * POST /api/inscription/create : créer un nouvel utilisateur (accessible admin uniquement en production)
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
        Users u = new Users(
                0,
                req.name(),
                req.mail(),
                req.psw(),
                req.isAdmin(),
                null,
                null
        );
        Users saved = inscriptionUseCase.saveUser(u);
        return ResponseEntity.ok(toResponse(saved));
    }

    /**
     * GET /api/inscription/users : récupérer tous les utilisateurs (accessible admin uniquement)
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<Users> users = inscriptionUseCase.getAllUsers();
        List<UserResponse> responses = users.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * DELETE /api/inscription/users/{id} : supprimer un utilisateur (accessible admin uniquement)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        inscriptionUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(Users user) {
        return new UserResponse(
                user.getIdUsers(),
                user.getName(),
                user.getMail(),
                user.getIsAdmin(),
                user.getPublicKey(),
                user.getVaultKey()
        );
    }

    public record CreateUserRequest(String name, String mail, String psw, boolean isAdmin) {}

    public record UserResponse(
            int idUsers,
            String name,
            String mail,
            boolean isAdmin,
            String publicKey,
            String vaultKey
    ) {}
}
