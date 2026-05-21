package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for note metadata response.
 * Returns note metadata to the client without exposing internal file paths.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String description;
    private String subject;
    private String semester;
    private String originalFileName;
    private Long fileSize;
    private String fileType;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}