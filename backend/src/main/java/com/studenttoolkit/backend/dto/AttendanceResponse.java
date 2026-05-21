package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for attendance history response.
 * Returns saved attendance data to the client with metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private String semesterName;
    private Double overallAttendancePercentage;
    private Integer totalClasses;
    private Integer totalAttended;
    private String subjectDetails;
    private LocalDateTime createdAt;
}