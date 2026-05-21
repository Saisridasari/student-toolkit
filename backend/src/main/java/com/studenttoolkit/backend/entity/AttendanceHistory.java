package com.studenttoolkit.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * AttendanceHistory entity stores attendance calculation records for a user.
 * Each record represents a semester's attendance with subject-wise details.
 * Linked to User via ManyToOne relationship - each user can have multiple attendance records.
 */
@Entity
@Table(name = "attendance_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String semesterName; // e.g., "Semester 1"

    @Column(nullable = false)
    private Double overallAttendancePercentage; // Overall attendance percentage for the semester

    @Column(nullable = false)
    private Integer totalClasses; // Total classes across all subjects

    @Column(nullable = false)
    private Integer totalAttended; // Total classes attended across all subjects

    @Column(nullable = false, length = 1000)
    private String subjectDetails; // JSON string storing subject-wise attendance

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}