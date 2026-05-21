package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.ProfileDto;
import com.studenttoolkit.backend.entity.ContactMessage;
import com.studenttoolkit.backend.entity.Feedback;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.ContactMessageRepository;
import com.studenttoolkit.backend.repository.FeedbackRepository;
import com.studenttoolkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin service providing dashboard statistics and management operations.
 * Only accessible by users with ADMIN role.
 * Provides aggregated data for the admin dashboard overview.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final FeedbackRepository feedbackRepository;

    /**
     * Get dashboard statistics for the admin overview page.
     * Returns aggregated counts and metrics about the system.
     * 
     * @return Map containing various statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);

        // Contact message statistics by status
        stats.put("pendingMessages", contactMessageRepository.countByStatus(ContactMessage.Status.PENDING));
        stats.put("inProgressMessages", contactMessageRepository.countByStatus(ContactMessage.Status.IN_PROGRESS));
        stats.put("resolvedMessages", contactMessageRepository.countByStatus(ContactMessage.Status.RESOLVED));

        // Feedback statistics
        long totalFeedbacks = feedbackRepository.count();
        stats.put("totalFeedbacks", totalFeedbacks);

        // Calculate average rating using the custom query
        Double avgRating = feedbackRepository.getAverageRating();
        stats.put("averageRating", avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0);

        log.info("Admin dashboard stats retrieved: {} users, {} feedbacks", totalUsers, totalFeedbacks);
        return stats;
    }

    /**
     * Get all registered users (admin only).
     * Returns a list of all user profiles for admin management.
     * 
     * @return List of ProfileDto objects
     */
    public List<ProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> ProfileDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .department(user.getDepartment())
                        .college(user.getCollege())
                        .semester(user.getSemester())
                        .role(user.getRole().name())
                        .active(user.getActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Toggle a user's active status (enable/disable account).
     * Used by admin to manage user accounts.
     * 
     * @param userId User ID to toggle
     * @return Updated ProfileDto
     */
    public ProfileDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(!user.getActive());
        User updated = userRepository.save(user);
        log.info("User {} status toggled to: {}", userId, updated.getActive());

        return ProfileDto.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .fullName(updated.getFullName())
                .phone(updated.getPhone())
                .department(updated.getDepartment())
                .college(updated.getCollege())
                .semester(updated.getSemester())
                .role(updated.getRole().name())
                .active(updated.getActive())
                .build();
    }
}