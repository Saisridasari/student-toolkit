package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.AttendanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AttendanceHistory entity providing CRUD operations and custom queries.
 * Allows users to save and retrieve their attendance calculation history.
 */
@Repository
public interface AttendanceHistoryRepository extends JpaRepository<AttendanceHistory, Long> {

    /**
     * Find all attendance history entries for a specific user.
     * Used to display a user's attendance history on their dashboard.
     */
    List<AttendanceHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find a specific attendance history entry by ID and user ID.
     * Ensures that a user can only access their own data.
     */
    AttendanceHistory findByIdAndUserId(Long id, Long userId);

    /**
     * Delete all attendance history entries for a specific user.
     * Used when a user wants to clear their attendance history.
     */
    void deleteByUserId(Long userId);
}