package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.FeedbackRequest;
import com.studenttoolkit.backend.dto.FeedbackResponse;
import com.studenttoolkit.backend.entity.Feedback;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Feedback service handling user feedback operations.
 * Users can submit feedback about the application.
 * Admins can view all feedback for analysis and improvement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AuthService authService;

    /**
     * Submit new feedback from a user.
     * 
     * @param email User's email (extracted from JWT token)
     * @param request Feedback data containing rating and comments
     * @return FeedbackResponse with the submitted feedback
     */
    @Transactional
    public FeedbackResponse submitFeedback(String email, FeedbackRequest request) {
        User user = authService.getCurrentUser(email);

        Feedback feedback = Feedback.builder()
                .user(user)
                .rating(request.getRating())
                .comments(request.getComments())
                .category(request.getCategory() != null ?
                        Feedback.Category.valueOf(request.getCategory()) :
                        Feedback.Category.GENERAL)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted by user: {}, rating: {}", email, request.getRating());

        return mapToResponse(saved);
    }

    /**
     * Get all feedback submitted by a specific user.
     * 
     * @param email User's email (extracted from JWT token)
     * @return List of FeedbackResponse objects
     */
    public List<FeedbackResponse> getUserFeedback(String email) {
        User user = authService.getCurrentUser(email);
        List<Feedback> feedbacks = feedbackRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all feedback (admin only).
     * Returns all feedback entries ordered by creation date.
     * 
     * @return List of FeedbackResponse objects
     */
    public List<FeedbackResponse> getAllFeedback() {
        List<Feedback> feedbacks = feedbackRepository.findAllByOrderByCreatedAtDesc();
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get feedback by category (admin only).
     * Used for filtering and analyzing feedback by type.
     * 
     * @param category Feedback category
     * @return List of FeedbackResponse objects matching the category
     */
    public List<FeedbackResponse> getFeedbackByCategory(Feedback.Category category) {
        List<Feedback> feedbacks = feedbackRepository.findByCategoryOrderByCreatedAtDesc(category);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a specific feedback entry.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id Feedback entry ID
     */
    @Transactional
    public void deleteFeedback(String email, Long id) {
        User user = authService.getCurrentUser(email);
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

        // Only the user who submitted the feedback or an admin can delete it
        if (!feedback.getUser().getId().equals(user.getId()) &&
                user.getRole() != User.Role.ADMIN) {
            throw new ResourceNotFoundException("Feedback", "id", id);
        }

        feedbackRepository.delete(feedback);
        log.info("Feedback deleted by user: {}, id: {}", email, id);
    }

    /**
     * Map Feedback entity to FeedbackResponse DTO.
     */
    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .userName(feedback.getUser().getFullName())
                .userEmail(feedback.getUser().getEmail())
                .rating(feedback.getRating())
                .comments(feedback.getComments())
                .category(feedback.getCategory() != null ? feedback.getCategory().name() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}