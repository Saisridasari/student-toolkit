package com.studenttoolkit.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for saving CGPA/SGPA calculation history.
 * Contains the semester GPA data that the user wants to save for future reference.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CGPARequest {

    @NotBlank(message = "Semester name is required")
    private String semesterName;

    @NotNull(message = "SGPA is required")
    @Min(value = 0, message = "SGPA must be at least 0")
    @Max(value = 10, message = "SGPA must be at most 10")
    private Double sgpa;

    @NotNull(message = "CGPA is required")
    @Min(value = 0, message = "CGPA must be at least 0")
    @Max(value = 10, message = "CGPA must be at most 10")
    private Double cgpa;

    @NotNull(message = "Total credits is required")
    @Min(value = 0, message = "Total credits must be at least 0")
    private Integer totalCredits;

    @NotNull(message = "Semester credits is required")
    @Min(value = 0, message = "Semester credits must be at least 0")
    private Integer semesterCredits;

    @NotBlank(message = "Grade details are required")
    private String gradeDetails; // JSON string with subject-wise grades
}