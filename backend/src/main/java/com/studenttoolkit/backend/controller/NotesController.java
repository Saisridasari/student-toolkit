package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.NoteRequest;
import com.studenttoolkit.backend.dto.NoteResponse;
import com.studenttoolkit.backend.entity.Note;
import com.studenttoolkit.backend.repository.NoteRepository;
import com.studenttoolkit.backend.service.NotesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Notes controller handling note metadata and file upload operations.
 * 
 * These endpoints require JWT authentication:
 * - POST /api/notes - Upload a new note with file
 * - GET /api/notes - Get current user's notes
 * - GET /api/notes/public - Get all public notes
 * - GET /api/notes/public?subject=Math - Filter public notes by subject
 * - PUT /api/notes/{id} - Update note metadata
 * - DELETE /api/notes/{id} - Delete a note
 * 
 * File uploads are handled via multipart form data.
 * The actual file is stored on the filesystem, and metadata is stored in the database.
 */
@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NotesController {

    private final NotesService notesService;
    private final NoteRepository noteRepository;

    // Directory where uploaded files are stored
    private static final String UPLOAD_DIR = "uploads/notes/";

    /**
     * Upload a new note with an associated file.
     * 
     * POST /api/notes
     * 
     * Headers: Authorization: Bearer <token>
     * Content-Type: multipart/form-data
     * 
     * Form fields:
     * - file: The actual file (PDF, DOCX, etc.)
     * - title: Note title
     * - description: Note description
     * - subject: Subject name
     * - semester: Semester number
     * - visibility: PRIVATE or PUBLIC
     */
    @PostMapping
    public ResponseEntity<ApiResponse> uploadNote(Authentication authentication,
                                                   @RequestParam("file") MultipartFile file,
                                                   @Valid NoteRequest request) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File cannot be empty"));
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename to prevent conflicts
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null && originalFileName.contains(".") ?
                    originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Save file to filesystem
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            String email = authentication.getName();
            String fileType = fileExtension.replace(".", "");
            if (fileType.isEmpty()) {
                fileType = "unknown";
            }

            // Save note metadata to database
            NoteResponse response = notesService.saveNote(
                    email, request,
                    filePath.toString(),
                    originalFileName,
                    file.getSize(),
                    fileType
            );

            return ResponseEntity.ok(ApiResponse.success("Note uploaded successfully", response));
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    /**
     * Get all notes for the current user.
     * 
     * GET /api/notes
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Returns both private and public notes of the user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getUserNotes(Authentication authentication) {
        String email = authentication.getName();
        List<NoteResponse> notes = notesService.getUserNotes(email);
        return ResponseEntity.ok(ApiResponse.success("User notes retrieved successfully", notes));
    }

    /**
     * Get all public notes (accessible by any authenticated user).
     * 
     * GET /api/notes/public
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Optional query parameter: subject (filter by subject)
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse> getPublicNotes(@RequestParam(required = false) String subject) {
        if (subject != null) {
            List<NoteResponse> notes = notesService.getPublicNotesBySubject(subject);
            return ResponseEntity.ok(ApiResponse.success("Public notes filtered by subject", notes));
        }
        List<NoteResponse> notes = notesService.getPublicNotes();
        return ResponseEntity.ok(ApiResponse.success("Public notes retrieved successfully", notes));
    }

    /**
     * Update note metadata (title, description, visibility, etc).
     * 
     * PUT /api/notes/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "title": "Updated Note Title",
     *   "description": "Updated description",
     *   "subject": "Mathematics",
     *   "semester": "4",
     *   "visibility": "PUBLIC"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateNote(Authentication authentication,
                                                    @PathVariable Long id,
                                                    @RequestBody NoteRequest request) {
        String email = authentication.getName();
        NoteResponse response = notesService.updateNote(email, id, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    /**
     * Delete a note and its associated file.
     *
     * DELETE /api/notes/{id}
     *
     * Headers: Authorization: Bearer <token>
     *
     * Users can only delete their own notes.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNote(Authentication authentication,
                                                    @PathVariable Long id) {
        String email = authentication.getName();

        // Get file path before deleting metadata so we can also delete the file
        String filePath = notesService.getNoteFilePath(id);
        notesService.deleteNote(email, id);

        // Also delete the physical file from filesystem
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            log.warn("Could not delete file at path: {}", filePath);
        }

        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully"));
    }

    /**
     * Download a note's associated file.
     *
     * GET /api/notes/{id}/download
     *
     * Headers: Authorization: Bearer <token>
     *
     * Returns the file as a downloadable attachment.
     * Users can download their own notes or any public note.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadNote(@PathVariable Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));

        try {
            Path filePath = Paths.get(note.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(note.getFileType());
            String originalFileName = note.getOriginalFileName();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("File path error for note id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * View a note's associated file inline (for PDF viewing in browser).
     *
     * GET /api/notes/{id}/view
     *
     * Headers: Authorization: Bearer <token>
     *
     * Returns the file with inline content-disposition so browsers can
     * render it directly (e.g., PDFs displayed in browser's PDF viewer).
     * Users can view their own notes or any public note.
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewNote(@PathVariable Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));

        try {
            Path filePath = Paths.get(note.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(note.getFileType());
            String originalFileName = note.getOriginalFileName();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + originalFileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("File path error for note id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determine the MIME content type based on file extension.
     */
    private String determineContentType(String fileType) {
        if (fileType == null) return "application/octet-stream";
        switch (fileType.toLowerCase()) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt": return "text/plain";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            default: return "application/octet-stream";
        }
    }
}