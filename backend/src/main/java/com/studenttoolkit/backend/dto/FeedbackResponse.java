package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for feedback response.
 * Returns feedback data to the client or admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String comments;
    private String category;
    private LocalDateTime createdAt;
}