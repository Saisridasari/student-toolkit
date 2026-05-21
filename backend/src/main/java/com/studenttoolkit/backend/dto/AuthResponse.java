package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response containing the JWT token.
 * Sent back to the client after successful login or registration.
 * The token should be included in the Authorization header of subsequent requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token; // JWT token for authentication
    private String email;
    private String fullName;
    private String role; // User role (STUDENT or ADMIN)
    private Long userId;
}