package com.studenttoolkit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notes metadata upload.
 * Contains the metadata for a note file being uploaded.
 * The actual file is handled separately via multipart upload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Semester is required")
    private String semester;

    private String visibility; // PRIVATE or PUBLIC
}