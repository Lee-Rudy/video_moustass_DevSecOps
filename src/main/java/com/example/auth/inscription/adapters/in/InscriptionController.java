package com.example.auth.inscription.adapters.in;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscription")
public class InscriptionController {

    private final InscriptionUseCase inscriptionUseCase;

    public InscriptionController(InscriptionUseCase inscriptionUseCase) {
        this.inscriptionUseCase = inscriptionUseCase;
    }

    @PostMapping("/create")
    public Users createUser(@RequestBody CreateUserRequest req) {
        Users u = new Users(
                0,
                req.name(),
                req.mail(),
                req.psw(),
                req.isAdmin(),
                null,
                null
        );
        return inscriptionUseCase.saveUser(u);
    }

    public record CreateUserRequest(String name, String mail, String psw, boolean isAdmin) {}
}
