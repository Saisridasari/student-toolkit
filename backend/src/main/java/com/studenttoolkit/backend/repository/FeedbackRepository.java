package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Feedback entity providing CRUD operations and custom queries.
 * Users can submit feedback; admins can view all feedback for analysis.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Find all feedback entries for a specific user.
     * Used to display a user's submitted feedback history.
     */
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all feedback entries ordered by creation date (newest first).
     * Used by admin to view all submitted feedback.
     */
    List<Feedback> findAllByOrderByCreatedAtDesc();

    /**
     * Find feedback by category - used by admin for category-wise analysis.
     */
    List<Feedback> findByCategoryOrderByCreatedAtDesc(Feedback.Category category);

    /**
     * Calculate average rating - used in admin dashboard statistics.
     * Uses a custom JPQL query to compute the average of all feedback ratings.
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();
}