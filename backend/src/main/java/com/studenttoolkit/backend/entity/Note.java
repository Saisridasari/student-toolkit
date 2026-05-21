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
 * Note entity stores metadata about uploaded notes/files.
 * The actual file is stored on the filesystem or cloud storage;
 * this entity only stores the metadata (file path, name, etc).
 * Linked to User via ManyToOne relationship - each user can upload multiple notes.
 */
@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(nullable = false, length = 20)
    private String semester;

    @Column(nullable = false, length = 500)
    private String filePath; // Path where the actual file is stored

    @Column(nullable = false, length = 100)
    private String originalFileName; // Original name of the uploaded file

    @Column(nullable = false)
    private Long fileSize; // File size in bytes

    @Column(nullable = false, length = 20)
    private String fileType; // e.g., "pdf", "docx", "jpg"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    /**
     * Visibility controls who can see the note.
     * PRIVATE - Only the owner can see it
     * PUBLIC - All users can see it
     */
    public enum Visibility {
        PRIVATE,
        PUBLIC
    }
}