package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.AttendanceRequest;
import com.studenttoolkit.backend.dto.AttendanceResponse;
import com.studenttoolkit.backend.entity.AttendanceHistory;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.AttendanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Attendance service handling attendance calculation history operations.
 * Users can save their attendance calculations and retrieve them later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceHistoryRepository attendanceHistoryRepository;
    private final AuthService authService;

    /**
     * Save a new attendance calculation to the user's history.
     * 
     * @param email User's email (extracted from JWT token)
     * @param request Attendance calculation data
     * @return AttendanceResponse with saved data
     */
    @Transactional
    public AttendanceResponse saveAttendance(String email, AttendanceRequest request) {
        User user = authService.getCurrentUser(email);

        AttendanceHistory history = AttendanceHistory.builder()
                .user(user)
                .semesterName(request.getSemesterName())
                .overallAttendancePercentage(request.getOverallAttendancePercentage())
                .totalClasses(request.getTotalClasses())
                .totalAttended(request.getTotalAttended())
                .subjectDetails(request.getSubjectDetails())
                .build();

        AttendanceHistory saved = attendanceHistoryRepository.save(history);
        log.info("Attendance history saved for user: {}, semester: {}", email, request.getSemesterName());

        return mapToResponse(saved);
    }

    /**
     * Get all attendance history entries for a user.
     * Returns entries ordered by creation date (newest first).
     * 
     * @param email User's email (extracted from JWT token)
     * @return List of AttendanceResponse objects
     */
    public List<AttendanceResponse> getAttendanceHistory(String email) {
        User user = authService.getCurrentUser(email);
        List<AttendanceHistory> histories = attendanceHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        return histories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific attendance history entry by ID.
     * Ensures the entry belongs to the requesting user.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id Attendance history entry ID
     * @return AttendanceResponse with the specific entry
     */
    public AttendanceResponse getAttendanceById(String email, Long id) {
        User user = authService.getCurrentUser(email);
        AttendanceHistory history = attendanceHistoryRepository.findByIdAndUserId(id, user.getId());

        if (history == null) {
            throw new ResourceNotFoundException("AttendanceHistory", "id", id);
        }

        return mapToResponse(history);
    }

    /**
     * Delete a specific attendance history entry.
     * Ensures the entry belongs to the requesting user.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id Attendance history entry ID
     */
    @Transactional
    public void deleteAttendance(String email, Long id) {
        User user = authService.getCurrentUser(email);
        AttendanceHistory history = attendanceHistoryRepository.findByIdAndUserId(id, user.getId());

        if (history == null) {
            throw new ResourceNotFoundException("AttendanceHistory", "id", id);
        }

        attendanceHistoryRepository.delete(history);
        log.info("Attendance history deleted for user: {}, id: {}", email, id);
    }

    /**
     * Map AttendanceHistory entity to AttendanceResponse DTO.
     * This conversion ensures we don't expose internal entity details.
     */
    private AttendanceResponse mapToResponse(AttendanceHistory history) {
        return AttendanceResponse.builder()
                .id(history.getId())
                .semesterName(history.getSemesterName())
                .overallAttendancePercentage(history.getOverallAttendancePercentage())
                .totalClasses(history.getTotalClasses())
                .totalAttended(history.getTotalAttended())
                .subjectDetails(history.getSubjectDetails())
                .createdAt(history.getCreatedAt())
                .build();
    }
}