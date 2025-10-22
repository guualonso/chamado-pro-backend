package com.chamado.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chamado.dto.LoginRequest;
import com.chamado.dto.TokenResponse;
import com.chamado.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        String token = authService.authenticateAndGetToken(request.getEmail(), request.getSenha());
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }
}
