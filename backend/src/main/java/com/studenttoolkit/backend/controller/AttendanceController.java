package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.AttendanceRequest;
import com.studenttoolkit.backend.dto.AttendanceResponse;
import com.studenttoolkit.backend.service.AttendanceService;
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
 * Attendance controller handling attendance calculation history operations.
 * 
 * These endpoints require JWT authentication:
 * - POST /api/attendance - Save a new attendance calculation
 * - GET /api/attendance - Get all attendance history for current user
 * - GET /api/attendance/{id} - Get a specific attendance history entry
 * - DELETE /api/attendance/{id} - Delete a specific attendance history entry
 * 
 * Users can only access and modify their own attendance history.
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Save a new attendance calculation to history.
     * 
     * POST /api/attendance
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "semesterName": "Semester 3",
     *   "overallAttendancePercentage": 85.5,
     *   "totalClasses": 100,
     *   "totalAttended": 86,
     *   "subjectDetails": "{\"Math\":{\"total\":30,\"attended\":28},\"Physics\":{\"total\":30,\"attended\":25}}"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse> saveAttendance(Authentication authentication,
                                                       @Valid @RequestBody AttendanceRequest request) {
        String email = authentication.getName();
        AttendanceResponse response = attendanceService.saveAttendance(email, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance history saved successfully", response));
    }

    /**
     * Get all attendance history entries for the current user.
     * 
     * GET /api/attendance
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Response: List of attendance history entries ordered by creation date (newest first)
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAttendanceHistory(Authentication authentication) {
        String email = authentication.getName();
        List<AttendanceResponse> history = attendanceService.getAttendanceHistory(email);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", history));
    }

    /**
     * Get a specific attendance history entry by ID.
     * 
     * GET /api/attendance/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Users can only access their own attendance history entries.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAttendanceById(Authentication authentication,
                                                          @PathVariable Long id) {
        String email = authentication.getName();
        AttendanceResponse response = attendanceService.getAttendanceById(email, id);
        return ResponseEntity.ok(ApiResponse.success("Attendance entry retrieved successfully", response));
    }

    /**
     * Delete a specific attendance history entry.
     * 
     * DELETE /api/attendance/{id}
     * 
     * Headers: Authorization: Bearer <token>
     * 
     * Users can only delete their own attendance history entries.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAttendance(Authentication authentication,
                                                         @PathVariable Long id) {
        String email = authentication.getName();
        attendanceService.deleteAttendance(email, id);
        return ResponseEntity.ok(ApiResponse.success("Attendance entry deleted successfully"));
    }
}