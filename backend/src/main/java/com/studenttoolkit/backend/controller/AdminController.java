package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.ProfileDto;
import com.studenttoolkit.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin controller providing dashboard and user management APIs.
 * 
 * ALL endpoints in this controller require ADMIN role.
 * The @PreAuthorize("hasRole('ADMIN')") annotation ensures only admins can access these endpoints.
 * 
 * Endpoints:
 * - GET /api/admin/dashboard - Get dashboard statistics
 * - GET /api/admin/users - Get all registered users
 * - PUT /api/admin/users/{id}/toggle-status - Enable/disable a user account
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // All endpoints require ADMIN role
public class AdminController {

    private final AdminService adminService;

    /**
     * Get dashboard statistics for the admin overview page.
     * 
     * GET /api/admin/dashboard
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Response includes:
     * - totalUsers: Total number of registered users
     * - pendingMessages: Number of pending contact messages
     * - inProgressMessages: Number of in-progress contact messages
     * - resolvedMessages: Number of resolved contact messages
     * - totalFeedbacks: Total number of feedback entries
     * - averageRating: Average feedback rating (1-5)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
    }

    /**
     * Get all registered users for admin management.
     * 
     * GET /api/admin/users
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Returns a list of all user profiles with their details.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        java.util.List<ProfileDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users retrieved successfully", users));
    }

    /**
     * Toggle a user's active status (enable/disable account).
     * 
     * PUT /api/admin/users/{id}/toggle-status
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Disabling a user prevents them from logging in (accountLocked in Spring Security).
     * Enabling re-activates their account.
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleUserStatus(@PathVariable Long id) {
        ProfileDto updatedUser = adminService.toggleUserStatus(id);
        String statusMessage = updatedUser.getActive() ? "User account enabled" : "User account disabled";
        return ResponseEntity.ok(ApiResponse.success(statusMessage, updatedUser));
    }
}