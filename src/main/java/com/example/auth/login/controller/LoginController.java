package com.example.auth.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.auth.login.models.LoginService;


@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginService.LoginResponse> login(@RequestBody LoginRequest req) {
        var opt = loginService.authenticate(req.mail(), req.password());
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(opt.get());
    }

    public record LoginRequest(String mail, String password) {}
}
