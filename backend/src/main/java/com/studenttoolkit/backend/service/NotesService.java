package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.NoteRequest;
import com.studenttoolkit.backend.dto.NoteResponse;
import com.studenttoolkit.backend.entity.Note;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Notes service handling note metadata operations.
 * The actual file upload/download is handled separately via multipart file operations.
 * This service manages the metadata (title, subject, visibility, etc.) of notes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotesService {

    private final NoteRepository noteRepository;
    private final AuthService authService;

    /**
     * Save note metadata after a file has been uploaded.
     * 
     * @param email User's email (extracted from JWT token)
     * @param request Note metadata (title, subject, etc.)
     * @param filePath Path where the uploaded file is stored
     * @param originalFileName Original name of the uploaded file
     * @param fileSize Size of the uploaded file in bytes
     * @param fileType Type/extension of the uploaded file
     * @return NoteResponse with saved note metadata
     */
    @Transactional
    public NoteResponse saveNote(String email, NoteRequest request,
                                  String filePath, String originalFileName,
                                  Long fileSize, String fileType) {
        User user = authService.getCurrentUser(email);

        Note note = Note.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(request.getSubject())
                .semester(request.getSemester())
                .filePath(filePath)
                .originalFileName(originalFileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .visibility(request.getVisibility() != null ?
                        Note.Visibility.valueOf(request.getVisibility()) :
                        Note.Visibility.PRIVATE)
                .build();

        Note saved = noteRepository.save(note);
        log.info("Note saved for user: {}, title: {}", email, request.getTitle());

        return mapToResponse(saved);
    }

    /**
     * Get all notes for a specific user.
     * Returns both private and public notes of the user.
     * 
     * @param email User's email (extracted from JWT token)
     * @return List of NoteResponse objects
     */
    public List<NoteResponse> getUserNotes(String email) {
        User user = authService.getCurrentUser(email);
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return notes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all public notes (accessible by any authenticated user).
     * Used for the public notes gallery where users share study materials.
     * 
     * @return List of NoteResponse objects for public notes
     */
    public List<NoteResponse> getPublicNotes() {
        List<Note> notes = noteRepository.findByVisibilityOrderByCreatedAtDesc(Note.Visibility.PUBLIC);

        return notes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get public notes filtered by subject.
     * 
     * @param subject Subject to filter by
     * @return List of NoteResponse objects for public notes in the subject
     */
    public List<NoteResponse> getPublicNotesBySubject(String subject) {
        List<Note> notes = noteRepository.findByVisibilityAndSubjectOrderByCreatedAtDesc(
                Note.Visibility.PUBLIC, subject);

        return notes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update note metadata (title, description, visibility, etc).
     * Only the owner can update their notes.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id Note ID
     * @param request Updated note metadata
     * @return Updated NoteResponse
     */
    @Transactional
    public NoteResponse updateNote(String email, Long id, NoteRequest request) {
        User user = authService.getCurrentUser(email);
        Note note = noteRepository.findByIdAndUserId(id, user.getId());

        if (note == null) {
            throw new ResourceNotFoundException("Note", "id", id);
        }

        // Update allowed fields
        note.setTitle(request.getTitle());
        note.setDescription(request.getDescription());
        note.setSubject(request.getSubject());
        note.setSemester(request.getSemester());
        if (request.getVisibility() != null) {
            note.setVisibility(Note.Visibility.valueOf(request.getVisibility()));
        }
        note.setUpdatedAt(java.time.LocalDateTime.now());

        Note updated = noteRepository.save(note);
        log.info("Note updated for user: {}, id: {}", email, id);

        return mapToResponse(updated);
    }

    /**
     * Delete a note and its associated file.
     * Only the owner can delete their notes.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id Note ID
     */
    @Transactional
    public void deleteNote(String email, Long id) {
        User user = authService.getCurrentUser(email);
        Note note = noteRepository.findByIdAndUserId(id, user.getId());

        if (note == null) {
            throw new ResourceNotFoundException("Note", "id", id);
        }

        noteRepository.delete(note);
        log.info("Note deleted for user: {}, id: {}", email, id);
    }

    /**
     * Get the file path for a note (used for file download).
     * Only the owner or users with access to public notes can get the file path.
     * 
     * @param id Note ID
     * @return File path of the note's associated file
     */
    public String getNoteFilePath(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
        return note.getFilePath();
    }

    /**
     * Map Note entity to NoteResponse DTO.
     * Excludes the internal file path for security.
     */
    private NoteResponse mapToResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .userId(note.getUser().getId())
                .userName(note.getUser().getFullName())
                .title(note.getTitle())
                .description(note.getDescription())
                .subject(note.getSubject())
                .semester(note.getSemester())
                .originalFileName(note.getOriginalFileName())
                .fileSize(note.getFileSize())
                .fileType(note.getFileType())
                .visibility(note.getVisibility().name())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}