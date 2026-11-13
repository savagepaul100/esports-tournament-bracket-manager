package com.esports.manager.controller;

import com.esports.manager.dto.Dtos.*;
import com.esports.manager.model.User;
import com.esports.manager.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = authService.authenticate(req.username, req.password);
        return userOpt.map(user -> ApiResponse.ok("Login successful", user))
                .orElseGet(() -> ApiResponse.error("Invalid credentials. Try admin / admin123"));
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest req) {
        try {
            User user = authService.register(req.username, req.email, req.password, req.role, req.gamerTag);
            return ApiResponse.ok("Account registered successfully", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<User> getMe(@RequestParam String userId) {
        return authService.getUserById(userId)
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.error("User not found"));
    }
}
