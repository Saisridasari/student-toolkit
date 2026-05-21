package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.config.JwtUtil;
import com.studenttoolkit.backend.dto.AuthResponse;
import com.studenttoolkit.backend.dto.LoginRequest;
import com.studenttoolkit.backend.dto.RegisterRequest;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.DuplicateResourceException;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service handling user registration and login.
 * 
 * Registration flow:
 * 1. Check if email already exists (prevent duplicate accounts)
 * 2. Encode the password using BCrypt (never store plain-text passwords)
 * 3. Save the user to the database
 * 4. Generate a JWT token for immediate authentication
 * 
 * Login flow:
 * 1. Authenticate the user using Spring Security's AuthenticationManager
 * 2. If credentials are valid, generate a JWT token
 * 3. Return the token along with user information
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user account.
     * 
     * @param request Registration data containing email, password, and profile info
     * @return AuthResponse with JWT token and user details
     * @throws DuplicateResourceException if email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if a user with this email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new user entity with BCrypt-encoded password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt encoding
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .college(request.getCollege())
                .semester(request.getSemester())
                .role(User.Role.STUDENT) // Default role is STUDENT
                .active(true)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate JWT token for the newly registered user
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Build and return the authentication response
        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .userId(savedUser.getId())
                .build();
    }

    /**
     * Authenticate a user and generate a JWT token.
     * 
     * @param request Login data containing email and password
     * @return AuthResponse with JWT token and user details
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate using Spring Security's AuthenticationManager
        // This will throw BadCredentialsException if email/password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Load user details after successful authentication
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);
        log.info("User logged in successfully: {}", user.getEmail());

        // Build and return the authentication response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    /**
     * Get the currently authenticated user from the security context.
     * Used by other services to identify the current user making the request.
     * 
     * @param email The email extracted from the JWT token
     * @return User entity from the database
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}