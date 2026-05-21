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
 * CGPAHistory entity stores the CGPA and SGPA calculations for a user.
 * Each record represents a semester's GPA calculation with the overall CGPA.
 * Linked to User via ManyToOne relationship - each user can have multiple history entries.
 */
@Entity
@Table(name = "cgpa_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CGPAHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String semesterName; // e.g., "Semester 1", "Semester 2"

    @Column(nullable = false)
    private Double sgpa; // Semester Grade Point Average for this specific semester

    @Column(nullable = false)
    private Double cgpa; // Cumulative Grade Point Average up to this semester

    @Column(nullable = false)
    private Integer totalCredits; // Total credits completed up to this semester

    @Column(nullable = false)
    private Integer semesterCredits; // Credits for this specific semester

    @Column(nullable = false, length = 500)
    private String gradeDetails; // JSON string storing subject-wise grades

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}