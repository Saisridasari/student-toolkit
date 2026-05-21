package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.CGPARequest;
import com.studenttoolkit.backend.dto.CGPAResponse;
import com.studenttoolkit.backend.service.CGPAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CGPA controller handling CGPA/SGPA calculation history operations.
 * 
 * These endpoints require JWT authentication:
 * - POST /api/cgpa - Save a new CGPA calculation
 * - GET /api/cgpa - Get all CGPA history for current user
 * - GET /api/cgpa/{id} - Get a specific CGPA history entry
 * - DELETE /api/cgpa/{id} - Delete a specific CGPA history entry
 * 
 * Users can only access and modify their own CGPA history.
 */
@Slf4j
@RestController
@RequestMapping("/api/cgpa")
@RequiredArgsConstructor
public class CGPAController {

    private final CGPAService cgpaService;

    /**
     * Save a new CGPA/SGPA calculation to history.
     * 
     * POST /api/cgpa
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "semesterName": "Semester 3",
     *   "sgpa": 8.5,
     *   "cgpa": 8.2,
     *   "totalCredits": 120,
     *   "semesterCredits": 24,
     *   "gradeDetails": "{\"Math\":\"A\",\"Physics\":\"B+\",\"Chemistry\":\"A-\"}"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse> saveCGPA(Authentication authentication,
                                                  @Valid @RequestBody CGPARequest request) {
        String email = authentication.getName();
        CGPAResponse response = cgpaService.saveCGPA(email, request);
        return ResponseEntity.ok(ApiResponse.success("CGPA history saved successfully", response));
    }

    /**
     * Get all CGPA history entries for the current user.
     * 
     * GET /api/cgpa
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Response: List of CGPA history entries ordered by creation date (newest first)
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getCGPAHistory(Authentication authentication) {
        String email = authentication.getName();
        List<CGPAResponse> history = cgpaService.getCGPAHistory(email);
        return ResponseEntity.ok(ApiResponse.success("CGPA history retrieved successfully", history));
    }

    /**
     * Get a specific CGPA history entry by ID.
     * 
     * GET /api/cgpa/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Users can only access their own CGPA history entries.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCGPAById(Authentication authentication,
                                                     @PathVariable Long id) {
        String email = authentication.getName();
        CGPAResponse response = cgpaService.getCGPAById(email, id);
        return ResponseEntity.ok(ApiResponse.success("CGPA entry retrieved successfully", response));
    }

    /**
     * Delete a specific CGPA history entry.
     * 
     * DELETE /api/cgpa/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Users can only delete their own CGPA history entries.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCGPA(Authentication authentication,
                                                    @PathVariable Long id) {
        String email = authentication.getName();
        cgpaService.deleteCGPA(email, id);
        return ResponseEntity.ok(ApiResponse.success("CGPA entry deleted successfully"));
    }
}