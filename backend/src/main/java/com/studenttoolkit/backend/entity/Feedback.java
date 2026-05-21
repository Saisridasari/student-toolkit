package com.studenttoolkit.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Feedback entity stores user feedback about the application.
 * Users can rate the app and provide comments for improvement.
 * Linked to User via ManyToOne relationship - each user can submit multiple feedbacks.
 */
@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // Rating from 1 to 5

    @Column(length = 2000)
    private String comments; // User's feedback comments

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category; // Type of feedback

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Categories for organizing feedback.
     * Helps admins filter and analyze feedback by type.
     */
    public enum Category {
        GENERAL,
        UI_UX,
        PERFORMANCE,
        FEATURE_REQUEST,
        BUG_REPORT,
        OTHER
    }
}