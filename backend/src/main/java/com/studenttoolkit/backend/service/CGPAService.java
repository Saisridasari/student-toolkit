package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.CGPARequest;
import com.studenttoolkit.backend.dto.CGPAResponse;
import com.studenttoolkit.backend.entity.CGPAHistory;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.CGPAHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CGPA service handling CGPA/SGPA calculation history operations.
 * Users can save their GPA calculations and retrieve them later for reference.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CGPAService {

    private final CGPAHistoryRepository cgpaHistoryRepository;
    private final AuthService authService;

    /**
     * Save a new CGPA/SGPA calculation to the user's history.
     * 
     * @param email User's email (extracted from JWT token)
     * @param request CGPA calculation data
     * @return CGPAResponse with saved data
     */
    @Transactional
    public CGPAResponse saveCGPA(String email, CGPARequest request) {
        User user = authService.getCurrentUser(email);

        CGPAHistory history = CGPAHistory.builder()
                .user(user)
                .semesterName(request.getSemesterName())
                .sgpa(request.getSgpa())
                .cgpa(request.getCgpa())
                .totalCredits(request.getTotalCredits())
                .semesterCredits(request.getSemesterCredits())
                .gradeDetails(request.getGradeDetails())
                .build();

        CGPAHistory saved = cgpaHistoryRepository.save(history);
        log.info("CGPA history saved for user: {}, semester: {}", email, request.getSemesterName());

        return mapToResponse(saved);
    }

    /**
     * Get all CGPA history entries for a user.
     * Returns entries ordered by creation date (newest first).
     * 
     * @param email User's email (extracted from JWT token)
     * @return List of CGPAResponse objects
     */
    public List<CGPAResponse> getCGPAHistory(String email) {
        User user = authService.getCurrentUser(email);
        List<CGPAHistory> histories = cgpaHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        return histories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific CGPA history entry by ID.
     * Ensures the entry belongs to the requesting user.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id CGPA history entry ID
     * @return CGPAResponse with the specific entry
     */
    public CGPAResponse getCGPAById(String email, Long id) {
        User user = authService.getCurrentUser(email);
        CGPAHistory history = cgpaHistoryRepository.findByIdAndUserId(id, user.getId());

        if (history == null) {
            throw new ResourceNotFoundException("CGPAHistory", "id", id);
        }

        return mapToResponse(history);
    }

    /**
     * Delete a specific CGPA history entry.
     * Ensures the entry belongs to the requesting user.
     * 
     * @param email User's email (extracted from JWT token)
     * @param id CGPA history entry ID
     */
    @Transactional
    public void deleteCGPA(String email, Long id) {
        User user = authService.getCurrentUser(email);
        CGPAHistory history = cgpaHistoryRepository.findByIdAndUserId(id, user.getId());

        if (history == null) {
            throw new ResourceNotFoundException("CGPAHistory", "id", id);
        }

        cgpaHistoryRepository.delete(history);
        log.info("CGPA history deleted for user: {}, id: {}", email, id);
    }

    /**
     * Map CGPAHistory entity to CGPAResponse DTO.
     * This conversion ensures we don't expose internal entity details.
     */
    private CGPAResponse mapToResponse(CGPAHistory history) {
        return CGPAResponse.builder()
                .id(history.getId())
                .semesterName(history.getSemesterName())
                .sgpa(history.getSgpa())
                .cgpa(history.getCgpa())
                .totalCredits(history.getTotalCredits())
                .semesterCredits(history.getSemesterCredits())
                .gradeDetails(history.getGradeDetails())
                .createdAt(history.getCreatedAt())
                .build();
    }
}