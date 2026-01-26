package com.example.auth.login.models;

import com.example.auth.config.JwtHelper;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final SpringDataUsersRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtHelper jwtHelper;

    public LoginService(SpringDataUsersRepository userRepo,
                        PasswordEncoder encoder,
                        JwtHelper jwtHelper) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtHelper = jwtHelper;
    }

    /**
     * Authentifie mail + mot de passe, renvoie token et userId si ok.
     */
    public Optional<LoginResponse> authenticate(String mail, String password) {
        if (mail == null || password == null) return Optional.empty();
        String mailNorm = mail.trim().toLowerCase();
        Optional<UsersJpaEntity> opt = userRepo.findByMail(mailNorm);
        if (opt.isEmpty()) return Optional.empty();
        UsersJpaEntity u = opt.get();
        if (!encoder.matches(password, u.getPswHash())) return Optional.empty();
        String token = jwtHelper.createToken(u.getId());
        String name = u.getName() != null ? u.getName() : "";
        return Optional.of(new LoginResponse(token, u.getId(), name));
    }

    public record LoginResponse(String token, Integer userId, String name) {}
}
