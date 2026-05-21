package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.ContactRequest;
import com.studenttoolkit.backend.dto.ContactResponse;
import com.studenttoolkit.backend.entity.ContactMessage;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contact service handling contact form submissions.
 * Anonymous users can submit contact messages.
 * Admin users can view, filter, and update the status of messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    /**
     * Submit a new contact message from the contact form.
     * This endpoint is public - no authentication required.
     * 
     * @param request Contact form data
     * @return ContactResponse with the submitted message details
     */
    @Transactional
    public ContactResponse submitContactMessage(ContactRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactMessage.Status.PENDING)
                .build();

        ContactMessage saved = contactMessageRepository.save(message);
        log.info("Contact message submitted from: {}", request.getEmail());

        return mapToResponse(saved);
    }

    /**
     * Get all contact messages (admin only).
     * Returns messages ordered by creation date (newest first).
     * 
     * @return List of ContactResponse objects
     */
    public List<ContactResponse> getAllMessages() {
        List<ContactMessage> messages = contactMessageRepository.findAllByOrderByCreatedAtDesc();
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get contact messages filtered by status (admin only).
     * 
     * @param status Message status (PENDING, IN_PROGRESS, RESOLVED)
     * @return List of ContactResponse objects matching the status
     */
    public List<ContactResponse> getMessagesByStatus(ContactMessage.Status status) {
        List<ContactMessage> messages = contactMessageRepository.findByStatusOrderByCreatedAtDesc(status);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update the status of a contact message (admin only).
     * Used to track the progress of resolving user inquiries.
     * 
     * @param id Message ID
     * @param status New status (PENDING, IN_PROGRESS, RESOLVED)
     * @return Updated ContactResponse
     */
    @Transactional
    public ContactResponse updateMessageStatus(Long id, ContactMessage.Status status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactMessage", "id", id));

        message.setStatus(status);

        // Set resolvedAt timestamp when message is resolved
        if (status == ContactMessage.Status.RESOLVED) {
            message.setResolvedAt(java.time.LocalDateTime.now());
        }

        ContactMessage updated = contactMessageRepository.save(message);
        log.info("Contact message {} status updated to: {}", id, status);

        return mapToResponse(updated);
    }

    /**
     * Get count of messages by status (admin dashboard statistics).
     * 
     * @param status Message status to count
     * @return Number of messages with the given status
     */
    public long countByStatus(ContactMessage.Status status) {
        return contactMessageRepository.countByStatus(status);
    }

    /**
     * Map ContactMessage entity to ContactResponse DTO.
     */
    private ContactResponse mapToResponse(ContactMessage message) {
        return ContactResponse.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .phone(message.getPhone())
                .subject(message.getSubject())
                .message(message.getMessage())
                .status(message.getStatus().name())
                .createdAt(message.getCreatedAt())
                .resolvedAt(message.getResolvedAt())
                .build();
    }
}