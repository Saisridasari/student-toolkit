package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.CGPAHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CGPAHistory entity providing CRUD operations and custom queries.
 * Allows users to save and retrieve their CGPA/SGPA calculation history.
 */
@Repository
public interface CGPAHistoryRepository extends JpaRepository<CGPAHistory, Long> {

    /**
     * Find all CGPA history entries for a specific user.
     * Used to display a user's GPA history on their dashboard.
     */
    List<CGPAHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find a specific CGPA history entry by ID and user ID.
     * Ensures that a user can only access their own data.
     */
    CGPAHistory findByIdAndUserId(Long id, Long userId);

    /**
     * Delete all CGPA history entries for a specific user.
     * Used when a user wants to clear their GPA history.
     */
    void deleteByUserId(Long userId);
}