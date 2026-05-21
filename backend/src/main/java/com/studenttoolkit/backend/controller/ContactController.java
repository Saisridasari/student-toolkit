package com.studenttoolkit.backend.controller;

import com.studenttoolkit.backend.dto.ApiResponse;
import com.studenttoolkit.backend.dto.ContactRequest;
import com.studenttoolkit.backend.dto.ContactResponse;
import com.studenttoolkit.backend.entity.ContactMessage;
import com.studenttoolkit.backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contact controller handling contact form submissions and admin message management.
 * 
 * Public endpoints (no authentication required):
 * - POST /api/contact - Submit a new contact message
 * 
 * Admin endpoints (require ADMIN role):
 * - GET /api/contact/messages - Get all contact messages
 * - GET /api/contact/messages?status=PENDING - Filter by status
 * - PUT /api/contact/messages/{id}/status - Update message status
 */
@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Submit a new contact message (public endpoint - no authentication required).
     * 
     * POST /api/contact
     * 
     * Request body example:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "phone": "9876543210",
     *   "subject": "Bug Report",
     *   "message": "I found a bug in the CGPA calculator..."
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse> submitContactMessage(@Valid @RequestBody ContactRequest request) {
        log.info("Contact message submitted from: {}", request.getEmail());
        ContactResponse response = contactService.submitContactMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Contact message submitted successfully", response));
    }

    /**
     * Get all contact messages (admin only).
     * 
     * GET /api/contact/messages
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Optional query parameter: status (PENDING, IN_PROGRESS, RESOLVED)
     */
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse> getMessages(@RequestParam(required = false) String status) {
        if (status != null) {
            ContactMessage.Status statusEnum = ContactMessage.Status.valueOf(status);
            List<ContactResponse> messages = contactService.getMessagesByStatus(statusEnum);
            return ResponseEntity.ok(ApiResponse.success("Messages filtered by status", messages));
        }
        List<ContactResponse> messages = contactService.getAllMessages();
        return ResponseEntity.ok(ApiResponse.success("All messages retrieved successfully", messages));
    }

    /**
     * Update the status of a contact message (admin only).
     * 
     * PUT /api/contact/messages/{id}/status?status=RESOLVED
     * 
     * Headers: Authorization: Bearer <admin-token>
     * 
     * Valid status values: PENDING, IN_PROGRESS, RESOLVED
     */
    @GetMapping("/messages/{id}/status")
    public ResponseEntity<ApiResponse> updateMessageStatus(@PathVariable Long id,
                                                            @RequestParam String status) {
        ContactMessage.Status statusEnum = ContactMessage.Status.valueOf(status);
        ContactResponse response = contactService.updateMessageStatus(id, statusEnum);
        return ResponseEntity.ok(ApiResponse.success("Message status updated successfully", response));
    }
}