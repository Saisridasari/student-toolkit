package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ContactMessage entity providing CRUD operations and custom queries.
 * Admin can view all messages; regular users can submit new messages.
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Find all contact messages ordered by creation date (newest first).
     * Used by admin to view all submitted contact messages.
     */
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    /**
     * Find contact messages by status - used by admin to filter messages.
     * PENDING, IN_PROGRESS, or RESOLVED status filtering.
     */
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.Status status);

    /**
     * Count messages by status - used in admin dashboard statistics.
     */
    long countByStatus(ContactMessage.Status status);
}