package com.studenttoolkit.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test controller providing health check and API information endpoints.
 * These endpoints are public (no authentication required) as defined in SecurityConfig.
 * Useful for verifying the backend is running and checking available API endpoints.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Health check endpoint to verify the backend is running.
     * 
     * GET /api/test
     * 
     * Returns basic server information including status, timestamp, and framework.
     */
    @GetMapping
    public Map<String, Object> testApi() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "Student Toolkit Backend API is running successfully!");
        response.put("timestamp", LocalDateTime.now());
        response.put("framework", "Spring Boot 3.3.0");
        response.put("javaVersion", System.getProperty("java.version"));
        return response;
    }

    /**
     * API documentation endpoint listing all available API routes.
     * 
     * GET /api/test/endpoints
     * 
     * Returns a map of all available API endpoints grouped by feature.
     */
    @GetMapping("/endpoints")
    public Map<String, Object> getEndpoints() {
        Map<String, Object> endpoints = new LinkedHashMap<>();

        // Authentication endpoints (public)
        Map<String, String> auth = new LinkedHashMap<>();
        auth.put("POST /api/auth/register", "Register a new user account");
        auth.put("POST /api/auth/login", "Login and get JWT token");
        endpoints.put("Authentication", auth);

        // Profile endpoints (authenticated)
        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("GET /api/profile", "Get current user's profile");
        profile.put("PUT /api/profile", "Update current user's profile");
        endpoints.put("Profile", profile);

        // CGPA endpoints (authenticated)
        Map<String, String> cgpa = new LinkedHashMap<>();
        cgpa.put("POST /api/cgpa", "Save CGPA/SGPA calculation");
        cgpa.put("GET /api/cgpa", "Get all CGPA history");
        cgpa.put("GET /api/cgpa/{id}", "Get specific CGPA entry");
        cgpa.put("DELETE /api/cgpa/{id}", "Delete CGPA entry");
        endpoints.put("CGPA", cgpa);

        // Attendance endpoints (authenticated)
        Map<String, String> attendance = new LinkedHashMap<>();
        attendance.put("POST /api/attendance", "Save attendance calculation");
        attendance.put("GET /api/attendance", "Get all attendance history");
        attendance.put("GET /api/attendance/{id}", "Get specific attendance entry");
        attendance.put("DELETE /api/attendance/{id}", "Delete attendance entry");
        endpoints.put("Attendance", attendance);

        // Notes endpoints (authenticated)
        Map<String, String> notes = new LinkedHashMap<>();
        notes.put("POST /api/notes", "Upload a note with file");
        notes.put("GET /api/notes", "Get user's notes");
        notes.put("GET /api/notes/public", "Get all public notes");
        notes.put("PUT /api/notes/{id}", "Update note metadata");
        notes.put("DELETE /api/notes/{id}", "Delete a note");
        endpoints.put("Notes", notes);

        // Contact endpoints (public + admin)
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("POST /api/contact", "Submit contact message (public)");
        contact.put("GET /api/contact/messages", "Get all messages (admin)");
        contact.put("GET /api/contact/messages?status=PENDING", "Filter messages by status (admin)");
        endpoints.put("Contact", contact);

        // Feedback endpoints (authenticated + admin)
        Map<String, String> feedback = new LinkedHashMap<>();
        feedback.put("POST /api/feedback", "Submit feedback");
        feedback.put("GET /api/feedback", "Get user's feedback");
        feedback.put("GET /api/feedback/all", "Get all feedback (admin)");
        feedback.put("DELETE /api/feedback/{id}", "Delete feedback");
        endpoints.put("Feedback", feedback);

        // Admin endpoints (admin only)
        Map<String, String> admin = new LinkedHashMap<>();
        admin.put("GET /api/admin/dashboard", "Get dashboard statistics");
        admin.put("GET /api/admin/users", "Get all users");
        admin.put("PUT /api/admin/users/{id}/toggle-status", "Enable/disable user");
        endpoints.put("Admin", admin);

        return endpoints;
    }
}
