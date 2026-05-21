package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Note entity providing CRUD operations and custom queries.
 * Users can save note metadata and retrieve their own or public notes.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Find all notes for a specific user ordered by creation date.
     * Used to display a user's personal notes on their dashboard.
     */
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all public notes ordered by creation date.
     * Used to display the public notes gallery for all users.
     */
    List<Note> findByVisibilityOrderByCreatedAtDesc(Note.Visibility visibility);

    /**
     * Find public notes by subject - used for subject-wise filtering.
     */
    List<Note> findByVisibilityAndSubjectOrderByCreatedAtDesc(Note.Visibility visibility, String subject);

    /**
     * Find a specific note by ID and user ID.
     * Ensures that a user can only modify their own notes.
     */
    Note findByIdAndUserId(Long id, Long userId);

    /**
     * Delete a note by ID and user ID.
     * Ensures that a user can only delete their own notes.
     */
    void deleteByIdAndUserId(Long id, Long userId);
}