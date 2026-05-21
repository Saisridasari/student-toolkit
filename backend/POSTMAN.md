# Student Toolkit Backend - Postman API Testing Guide

This document provides comprehensive Postman testing examples for all API endpoints.

## Setup

1. **Base URL**: `http://localhost:8080`
2. **Authentication**: Most endpoints require a JWT token in the Authorization header.
3. **Header Format**: `Authorization: Bearer <token>`

---

## 1. Authentication APIs

### Register a New User

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "email": "student@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phone": "9876543210",
    "department": "Computer Science",
    "college": "MIT",
    "semester": "3"
}
```

**Response:**
```json
{
    "success": true,
    "message": "User registered successfully",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "email": "student@example.com",
        "fullName": "John Doe",
        "role": "STUDENT",
        "userId": 1
    }
}
```

### Login

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "student@example.com",
    "password": "password123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "email": "student@example.com",
        "fullName": "John Doe",
        "role": "STUDENT",
        "userId": 1
    }
}
```

> **Important**: Copy the `token` value from the response. You'll need it for all authenticated endpoints.

---

## 2. Profile APIs

> **Headers required**: `Authorization: Bearer <your-token>`

### Get Profile

```
GET http://localhost:8080/api/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
    "success": true,
    "message": "Profile retrieved successfully",
    "data": {
        "id": 1,
        "email": "student@example.com",
        "fullName": "John Doe",
        "phone": "9876543210",
        "department": "Computer Science",
        "college": "MIT",
        "semester": "3",
        "role": "STUDENT",
        "active": true
    }
}
```

### Update Profile

```
PUT http://localhost:8080/api/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "fullName": "John Doe Updated",
    "phone": "1234567890",
    "department": "Electrical Engineering",
    "college": "Stanford",
    "semester": "5"
}
```

---

## 3. CGPA/SGPA APIs

> **Headers required**: `Authorization: Bearer <your-token>`

### Save CGPA Calculation

```
POST http://localhost:8080/api/cgpa
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "semesterName": "Semester 3",
    "sgpa": 8.5,
    "cgpa": 8.2,
    "totalCredits": 120,
    "semesterCredits": 24,
    "gradeDetails": "{\"Math\":\"A\",\"Physics\":\"B+\",\"Chemistry\":\"A-\",\"English\":\"A\"}"
}
```

**Response:**
```json
{
    "success": true,
    "message": "CGPA history saved successfully",
    "data": {
        "id": 1,
        "semesterName": "Semester 3",
        "sgpa": 8.5,
        "cgpa": 8.2,
        "totalCredits": 120,
        "semesterCredits": 24,
        "gradeDetails": "{\"Math\":\"A\",\"Physics\":\"B+\",\"Chemistry\":\"A-\",\"English\":\"A\"}",
        "createdAt": "2024-01-15T10:30:00"
    }
}
```

### Get All CGPA History

```
GET http://localhost:8080/api/cgpa
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Get Specific CGPA Entry

```
GET http://localhost:8080/api/cgpa/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Delete CGPA Entry

```
DELETE http://localhost:8080/api/cgpa/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 4. Attendance APIs

> **Headers required**: `Authorization: Bearer <your-token>`

### Save Attendance Calculation

```
POST http://localhost:8080/api/attendance
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "semesterName": "Semester 3",
    "overallAttendancePercentage": 85.5,
    "totalClasses": 100,
    "totalAttended": 86,
    "subjectDetails": "{\"Math\":{\"total\":30,\"attended\":28},\"Physics\":{\"total\":30,\"attended\":25},\"Chemistry\":{\"total\":20,\"attended\":18},\"English\":{\"total\":20,\"attended\":15}}"
}
```

### Get All Attendance History

```
GET http://localhost:8080/api/attendance
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Get Specific Attendance Entry

```
GET http://localhost:8080/api/attendance/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Delete Attendance Entry

```
DELETE http://localhost:8080/api/attendance/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 5. Notes APIs

> **Headers required**: `Authorization: Bearer <your-token>`

### Upload a Note (multipart form-data)

```
POST http://localhost:8080/api/notes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data

Form fields:
- file: [Select a PDF/DOCX file from your computer]
- title: "Data Structures Notes"
- description: "Complete notes for DS semester 3"
- subject: "Data Structures"
- semester: "3"
- visibility: "PUBLIC"
```

> **In Postman**: Select "form-data" body type, add a "file" field with type "File", and text fields for other parameters.

### Get User's Notes

```
GET http://localhost:8080/api/notes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Get Public Notes

```
GET http://localhost:8080/api/notes/public
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Get Public Notes by Subject

```
GET http://localhost:8080/api/notes/public?subject=Data%20Structures
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Update Note Metadata

```
PUT http://localhost:8080/api/notes/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "title": "Updated DS Notes",
    "description": "Updated description",
    "subject": "Data Structures",
    "semester": "3",
    "visibility": "PUBLIC"
}
```

### Delete a Note

```
DELETE http://localhost:8080/api/notes/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 6. Contact Form API (Public - No Auth Required)

### Submit Contact Message

```
POST http://localhost:8080/api/contact
Content-Type: application/json

{
    "name": "Jane Smith",
    "email": "jane@example.com",
    "phone": "9876543210",
    "subject": "Bug Report",
    "message": "I found a bug in the CGPA calculator. It doesn't handle zero credits correctly."
}
```

**Response:**
```json
{
    "success": true,
    "message": "Contact message submitted successfully",
    "data": {
        "id": 1,
        "name": "Jane Smith",
        "email": "jane@example.com",
        "phone": "9876543210",
        "subject": "Bug Report",
        "message": "I found a bug...",
        "status": "PENDING",
        "createdAt": "2024-01-15T10:30:00",
        "resolvedAt": null
    }
}
```

---

## 7. Feedback APIs

> **Headers required**: `Authorization: Bearer <your-token>`

### Submit Feedback

```
POST http://localhost:8080/api/feedback
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "rating": 4,
    "comments": "Great app! The CGPA calculator is very useful.",
    "category": "GENERAL"
}
```

> **Valid categories**: GENERAL, UI_UX, PERFORMANCE, FEATURE_REQUEST, BUG_REPORT, OTHER
> **Rating range**: 1-5

### Get User's Feedback

```
GET http://localhost:8080/api/feedback
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Delete Feedback

```
DELETE http://localhost:8080/api/feedback/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 8. Admin APIs (Admin Role Required)

> **Headers required**: `Authorization: Bearer <admin-token>`
> **Note**: You need an admin account to access these endpoints. Create one by setting role to ADMIN in the database.

### Get Dashboard Statistics

```
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer <admin-token>
```

**Response:**
```json
{
    "success": true,
    "message": "Dashboard stats retrieved successfully",
    "data": {
        "totalUsers": 150,
        "pendingMessages": 5,
        "inProgressMessages": 3,
        "resolvedMessages": 20,
        "totalFeedbacks": 45,
        "averageRating": 4.2
    }
}
```

### Get All Users

```
GET http://localhost:8080/api/admin/users
Authorization: Bearer <admin-token>
```

### Toggle User Status (Enable/Disable)

```
PUT http://localhost:8080/api/admin/users/1/toggle-status
Authorization: Bearer <admin-token>
```

### Get All Contact Messages (Admin)

```
GET http://localhost:8080/api/contact/messages
Authorization: Bearer <admin-token>
```

### Filter Messages by Status (Admin)

```
GET http://localhost:8080/api/contact/messages?status=PENDING
Authorization: Bearer <admin-token>
```

### Update Message Status (Admin)

```
GET http://localhost:8080/api/contact/messages/1/status?status=RESOLVED
Authorization: Bearer <admin-token>
```

### Get All Feedback (Admin)

```
GET http://localhost:8080/api/feedback/all
Authorization: Bearer <admin-token>
```

### Filter Feedback by Category (Admin)

```
GET http://localhost:8080/api/feedback/all?category=BUG_REPORT
Authorization: Bearer <admin-token>
```

---

## 9. Test/Health Check APIs (Public)

### Health Check

```
GET http://localhost:8080/api/test
```

**Response:**
```json
{
    "status": "UP",
    "message": "Student Toolkit Backend API is running successfully!",
    "timestamp": "2024-01-15T10:30:00",
    "framework": "Spring Boot 3.3.0",
    "javaVersion": "17"
}
```

### List All Endpoints

```
GET http://localhost:8080/api/test/endpoints
```

---

## Postman Collection Setup Tips

1. **Create a Collection**: Name it "Student Toolkit Backend"
2. **Set Collection Variables**:
   - `base_url`: `http://localhost:8080`
   - `token`: (set after login/register response)
3. **Use Variables in Requests**:
   - URL: `{{base_url}}/api/auth/login`
   - Authorization: `Bearer {{token}}`
4. **Add Tests to Auto-Set Token**:
   - In the Login request's "Tests" tab, add:
   ```javascript
   const response = pm.response.json();
   if (response.success && response.data.token) {
       pm.collectionVariables.set("token", response.data.token);
   }
   ```
5. **This way, after login, all subsequent requests automatically use the token.**

---

## Common Error Responses

### 401 Unauthorized (Missing/Invalid Token)
```json
{
    "success": false,
    "message": "Authentication failed: Full authentication is required",
    "data": null
}
```

### 403 Forbidden (Non-Admin Accessing Admin Endpoint)
```json
{
    "success": false,
    "message": "Access Denied",
    "data": null
}
```

### 404 Not Found
```json
{
    "success": false,
    "message": "CGPAHistory not found with id: 999",
    "data": null
}
```

### 409 Conflict (Duplicate Email)
```json
{
    "success": false,
    "message": "User already exists with email: student@example.com",
    "data": null
}
```

### 400 Bad Request (Validation Error)
```json
{
    "success": false,
    "message": "Validation failed: {email=Email is required, password=Password must be between 6 and 100 characters}",
    "data": null
}