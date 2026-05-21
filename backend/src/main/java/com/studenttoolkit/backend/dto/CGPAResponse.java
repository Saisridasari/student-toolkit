package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for CGPA history response.
 * Returns saved CGPA/SGPA data to the client with metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CGPAResponse {

    private Long id;
    private String semesterName;
    private Double sgpa;
    private Double cgpa;
    private Integer totalCredits;
    private Integer semesterCredits;
    private String gradeDetails;
    private LocalDateTime createdAt;
}