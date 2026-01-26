package com.example.auth.user;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GET /api/users : liste des utilisateurs non-admin (pour le champ « Envoyé à »).
 * Authentification JWT requise.
 */
@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final SpringDataUsersRepository userRepo;

    public UsersController(SpringDataUsersRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listNonAdmin(@RequestAttribute("userId") Integer userId) {
        List<UsersJpaEntity> list = userRepo.findByIsAdminFalse();
        List<UserDto> dtos = list.stream()
                .map(u -> new UserDto(u.getId(), u.getName() != null ? u.getName() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public record UserDto(int id, String name) {}
}
