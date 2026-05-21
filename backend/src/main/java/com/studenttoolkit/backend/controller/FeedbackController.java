package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.FeedbackRequest;
import com.studenttoolkit.backend.dto.FeedbackResponse;
import com.studenttoolkit.backend.entity.Feedback;
import com.studenttoolkit.backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Feedback controller handling user feedback operations.
 * 
 * Authenticated endpoints (require JWT token):
 * - POST /api/feedback - Submit new feedback
 * - GET /api/feedback - Get current user's feedback history
 * - DELETE /api/feedback/{id} - Delete a feedback entry
 * 
 * Admin endpoints (require ADMIN role):
 * - GET /api/feedback/all - Get all feedback
 * - GET /api/feedback/all?category=BUG_REPORT - Filter by category
 */
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Submit new feedback.
     * 
     * POST /api/feedback
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "rating": 4,
     *   "comments": "Great app! The CGPA calculator is very useful.",
     *   "category": "GENERAL"
     * }
     * 
     * Valid categories: GENERAL, UI_UX, PERFORMANCE, FEATURE_REQUEST, BUG_REPORT, OTHER
     * Rating must be between 1 and 5.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> submitFeedback(Authentication authentication,
                                                       @Valid @RequestBody FeedbackRequest request) {
        String email = authentication.getName();
        FeedbackResponse response = feedbackService.submitFeedback(email, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully", response));
    }

    /**
     * Get current user's feedback history.
     * 
     * GET /api/feedback
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Returns all feedback entries submitted by the current user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getUserFeedback(Authentication authentication) {
        String email = authentication.getName();
        List<FeedbackResponse> feedbacks = feedbackService.getUserFeedback(email);
        return ResponseEntity.ok(ApiResponse.success("User feedback retrieved successfully", feedbacks));
    }

    /**
     * Get all feedback (admin only).
     * 
     * GET /api/feedback/all
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Optional query parameter: category (filter by feedback category)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllFeedback(@RequestParam(required = false) String category) {
        if (category != null) {
            Feedback.Category categoryEnum = Feedback.Category.valueOf(category);
            List<FeedbackResponse> feedbacks = feedbackService.getFeedbackByCategory(categoryEnum);
            return ResponseEntity.ok(ApiResponse.success("Feedback filtered by category", feedbacks));
        }
        List<FeedbackResponse> feedbacks = feedbackService.getAllFeedback();
        return ResponseEntity.ok(ApiResponse.success("All feedback retrieved successfully", feedbacks));
    }

    /**
     * Delete a feedback entry.
     * 
     * DELETE /api/feedback/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Users can delete their own feedback. Admins can delete any feedback.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteFeedback(Authentication authentication,
                                                       @PathVariable Long id) {
        String email = authentication.getName();
        feedbackService.deleteFeedback(email, id);
        return ResponseEntity.ok(ApiResponse.success("Feedback deleted successfully"));
    }
}