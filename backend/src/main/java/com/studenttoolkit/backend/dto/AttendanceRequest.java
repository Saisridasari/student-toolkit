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
 * DTO for saving attendance calculation history.
 * Contains the attendance data that the user wants to save for future reference.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotBlank(message = "Semester name is required")
    private String semesterName;

    @NotNull(message = "Overall attendance percentage is required")
    @Min(value = 0, message = "Attendance must be at least 0%")
    @Max(value = 100, message = "Attendance must be at most 100%")
    private Double overallAttendancePercentage;

    @NotNull(message = "Total classes is required")
    @Min(value = 0, message = "Total classes must be at least 0")
    private Integer totalClasses;

    @NotNull(message = "Total attended is required")
    @Min(value = 0, message = "Total attended must be at least 0")
    private Integer totalAttended;

    @NotBlank(message = "Subject details are required")
    private String subjectDetails; // JSON string with subject-wise attendance
}