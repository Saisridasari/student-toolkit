package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.ProfileDto;
import com.studenttoolkit.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile controller handling user profile operations.
 * 
 * These endpoints require JWT authentication:
 * - GET /api/profile - Get current user's profile
 * - PUT /api/profile - Update current user's profile
 * 
 * The user's email is extracted from the JWT token in the Authentication object,
 * so users can only access and modify their own profile.
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get the current user's profile.
     * 
     * GET /api/profile
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Response: User profile details (email, name, department, etc.)
     * Password is never included in the response.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        String email = authentication.getName(); // Extracted from JWT token
        ProfileDto profile = profileService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * Update the current user's profile.
     * 
     * PUT /api/profile
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "fullName": "John Doe Updated",
     *   "phone": "9876543210",
     *   "department": "Electrical Engineering",
     *   "college": "Stanford",
     *   "semester": "5"
     * }
     * 
     * Note: Email and password cannot be changed through this endpoint.
     */
    @PutMapping
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication,
                                                      @RequestBody ProfileDto profileDto) {
        String email = authentication.getName(); // Extracted from JWT token
        ProfileDto updatedProfile = profileService.updateProfile(email, profileDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }
}