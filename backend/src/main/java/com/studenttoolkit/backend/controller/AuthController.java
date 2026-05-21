package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.AuthResponse;
import com.studenttoolkit.backend.dto.LoginRequest;
import com.studenttoolkit.backend.dto.RegisterRequest;
import com.studenttoolkit.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller handling user registration and login.
 * 
 * These endpoints are public (no JWT token required) as defined in SecurityConfig:
 * - POST /api/auth/register - Create a new user account
 * - POST /api/auth/login - Authenticate and get a JWT token
 * 
 * After successful registration or login, the client receives a JWT token
 * that must be included in the Authorization header of all subsequent requests:
 * Authorization: Bearer <token>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     * 
     * POST /api/auth/register
     * 
     * Request body example:
     * {
     *   "email": "student@example.com",
     *   "password": "password123",
     *   "fullName": "John Doe",
     *   "phone": "9876543210",
     *   "department": "Computer Science",
     *   "college": "MIT",
     *   "semester": "3"
     * }
     * 
     * Response: JWT token with user details for immediate authentication
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
    }

    /**
     * Login with existing credentials.
     * 
     * POST /api/auth/login
     * 
     * Request body example:
     * {
     *   "email": "student@example.com",
     *   "password": "password123"
     * }
     * 
     * Response: JWT token with user details for authentication
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}