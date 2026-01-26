package com.example.auth.login.controller;

import com.example.auth.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.auth.login.models.LoginService;


@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final LoginService loginService;
    private final AuditLogService auditLogService;

    public LoginController(LoginService loginService, AuditLogService auditLogService) {
        this.loginService = loginService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<LoginService.LoginResponse> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        var opt = loginService.authenticate(req.mail(), req.password());
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        // Log de la connexion
        LoginService.LoginResponse response = opt.get();
        String message = String.format("Connexion réussie (userId: %d, name: %s)", response.userId(), response.name());
        auditLogService.logAction(response.userId(), "USER_LOGIN", "users", response.userId(), message, request);
        
        return ResponseEntity.ok(response);
    }

    public record LoginRequest(String mail, String password) {}
}
