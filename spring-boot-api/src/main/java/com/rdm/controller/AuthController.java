package com.rdm.controller;

import com.rdm.dto.LoginRequest;
import com.rdm.dto.LoginResponse;
import com.rdm.dto.UserDTO;
import com.rdm.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT is stateless, so logout is handled on client side by removing token
        return ResponseEntity.ok("Logged out successfully");
    }
}

