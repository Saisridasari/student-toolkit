# Student Toolkit Backend

A production-ready Spring Boot backend for the Student Toolkit web application, featuring JWT authentication, MySQL database, and comprehensive REST APIs.

## 🏗 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/studenttoolkit/backend/
│   │   │   ├── config/                    # Configuration classes
│   │   │   │   ├── CorsConfig.java         # CORS configuration (handled in SecurityConfig)
│   │   │   │   ├── JwtAuthenticationFilter.java  # JWT token validation filter
│   │   │   │   ├── JwtUtil.java             # JWT token generation & validation
│   │   │   │   └── SecurityConfig.java      # Spring Security + JWT + CORS config
│   │   │   ├── controller/                 # REST API controllers
│   │   │   │   ├── AuthController.java      # Login & Registration
│   │   │   │   ├── ProfileController.java   # User profile operations
│   │   │   │   ├── CGPAController.java      # CGPA/SGPA history
│   │   │   │   ├── AttendanceController.java # Attendance history
│   │   │   │   ├── NotesController.java     # Notes upload & metadata
│   │   │   │   ├── ContactController.java   # Contact form
│   │   │   │   ├── FeedbackController.java  # User feedback
│   │   │   │   ├── AdminController.java     # Admin dashboard APIs
│   │   │   │   └── TestController.java      # Health check & API docs
│   │   │   ├── dto/                        # Data Transfer Objects
│   │   │   │   ├── RegisterRequest.java     # Registration request DTO
│   │   │   │   ├── LoginRequest.java        # Login request DTO
│   │   │   │   ├── AuthResponse.java        # Auth response with JWT token
│   │   │   │   ├── ProfileDto.java          # Profile data DTO
│   │   │   │   ├── CGPARequest.java         # CGPA save request DTO
│   │   │   │   ├── CGPAResponse.java        # CGPA response DTO
│   │   │   │   ├── AttendanceRequest.java   # Attendance save request DTO
│   │   │   │   ├── AttendanceResponse.java  # Attendance response DTO
│   │   │   │   ├── ContactRequest.java      # Contact form request DTO
│   │   │   │   ├── ContactResponse.java     # Contact response DTO
│   │   │   │   ├── FeedbackRequest.java     # Feedback request DTO
│   │   │   │   ├── FeedbackResponse.java    # Feedback response DTO
│   │   │   │   ├── NoteRequest.java         # Note metadata request DTO
│   │   │   │   ├── NoteResponse.java        # Note response DTO
│   │   │   │   └── ApiResponse.java         # Generic API response wrapper
│   │   │   ├── entity/                      # JPA Entity classes
│   │   │   │   ├── User.java                # User entity (with Role enum)
│   │   │   │   ├── CGPAHistory.java         # CGPA/SGPA history entity
│   │   │   │   ├── AttendanceHistory.java   # Attendance history entity
│   │   │   │   ├── ContactMessage.java      # Contact form message entity
│   │   │   │   ├── Note.java                # Note metadata entity
│   │   │   │   └── Feedback.java            # Feedback entity
│   │   │   ├── exception/                   # Custom exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── GlobalExceptionHandler.java  # Global error handler
│   │   │   ├── repository/                  # Spring Data JPA repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CGPAHistoryRepository.java
│   │   │   │   ├── AttendanceHistoryRepository.java
│   │   │   │   ├── ContactMessageRepository.java
│   │   │   │   ├── NoteRepository.java
│   │   │   │   └── FeedbackRepository.java
│   │   │   ├── service/                     # Business logic services
│   │   │   │   ├── AuthService.java         # Authentication service
│   │   │   │   ├── ProfileService.java      # Profile management service
│   │   │   │   ├── CGPAService.java         # CGPA history service
│   │   │   │   ├── AttendanceService.java   # Attendance history service
│   │   │   │   ├── ContactService.java      # Contact form service
│   │   │   │   ├── FeedbackService.java     # Feedback service
│   │   │   │   ├── NotesService.java        # Notes metadata service
│   │   │   │   └── AdminService.java        # Admin dashboard service
│   │   │   └── BackendApplication.java      # Main entry point
│   │   └── resources/
│   │       └── application.properties        # Configuration file
│   └── test/
│       ├── java/com/studenttoolkit/backend/
│       │   └── BackendApplicationTests.java
│       └── resources/
│           └── application.properties          # Test config (H2 database)
├── pom.xml                                 # Maven dependencies
├── POSTMAN.md                              # Postman testing guide
└── README.md                               # This file
```

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+** (running on localhost:3306)

### 1. Setup MySQL Database

```sql
-- The database is auto-created by Spring Boot (createDatabaseIfNotExist=true)
-- But you can also create it manually:
CREATE DATABASE student_toolkit_db;
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_toolkit_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 3. Build & Run

```bash
# Navigate to the backend directory
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

### 4. Verify It's Running

```bash
# Health check
curl http://localhost:8080/api/test

# List all endpoints
curl http://localhost:8080/api/test/endpoints
```

## 🔐 Authentication Flow

1. **Register**: `POST /api/auth/register` → Receive JWT token
2. **Login**: `POST /api/auth/login` → Receive JWT token
3. **Use Token**: Add `Authorization: Bearer <token>` header to all authenticated requests

### Create an Admin User

After registering a normal user, update their role in MySQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

## 📡 API Endpoints Summary

| Category | Endpoint | Method | Auth | Description |
|-----------|----------|--------|------|-------------|
| **Auth** | `/api/auth/register` | POST | Public | Register new user |
| | `/api/auth/login` | POST | Public | Login & get JWT token |
| **Profile** | `/api/profile` | GET | User | Get user profile |
| | `/api/profile` | PUT | User | Update user profile |
| **CGPA** | `/api/cgpa` | POST | User | Save CGPA calculation |
| | `/api/cgpa` | GET | User | Get CGPA history |
| | `/api/cgpa/{id}` | GET | User | Get specific CGPA entry |
| | `/api/cgpa/{id}` | DELETE | User | Delete CGPA entry |
| **Attendance** | `/api/attendance` | POST | User | Save attendance calculation |
| | `/api/attendance` | GET | User | Get attendance history |
| | `/api/attendance/{id}` | GET | User | Get specific entry |
| | `/api/attendance/{id}` | DELETE | User | Delete entry |
| **Notes** | `/api/notes` | POST | User | Upload note with file |
| | `/api/notes` | GET | User | Get user's notes |
| | `/api/notes/public` | GET | User | Get public notes |
| | `/api/notes/{id}` | PUT | User | Update note metadata |
| | `/api/notes/{id}` | DELETE | User | Delete note |
| **Contact** | `/api/contact` | POST | Public | Submit contact message |
| | `/api/contact/messages` | GET | Admin | Get all messages |
| | `/api/contact/messages/{id}/status` | GET | Admin | Update message status |
| **Feedback** | `/api/feedback` | POST | User | Submit feedback |
| | `/api/feedback` | GET | User | Get user's feedback |
| | `/api/feedback/all` | GET | Admin | Get all feedback |
| | `/api/feedback/{id}` | DELETE | User | Delete feedback |
| **Admin** | `/api/admin/dashboard` | GET | Admin | Dashboard statistics |
| | `/api/admin/users` | GET | Admin | Get all users |
| | `/api/admin/users/{id}/toggle-status` | PUT | Admin | Enable/disable user |
| **Test** | `/api/test` | GET | Public | Health check |
| | `/api/test/endpoints` | GET | Public | List all endpoints |

## 🗄️ Database Schema

Spring Boot automatically creates all tables using Hibernate's `ddl-auto=update` setting. The main tables are:

- **users** - User accounts with email, password (BCrypt), role (STUDENT/ADMIN)
- **cgpa_history** - CGPA/SGPA calculations linked to users
- **attendance_history** - Attendance calculations linked to users
- **contact_messages** - Contact form submissions with status tracking
- **notes** - Note metadata with file paths and visibility settings
- **feedbacks** - User feedback with ratings and categories

## 🔒 Security Architecture

- **Spring Security** with JWT-based stateless authentication
- **BCrypt** password encoding (never stores plain-text passwords)
- **JWT tokens** expire after 24 hours (configurable)
- **Role-based access**: STUDENT (default) and ADMIN roles
- **CORS** enabled for frontend integration
- **No sessions** - fully stateless architecture

## 🧪 Testing

```bash
# Run all tests (uses H2 in-memory database, no MySQL needed)
mvn test
```

## 📮 Postman Testing

See [POSTMAN.md](POSTMAN.md) for comprehensive API testing examples with request/response formats.

## 🔧 Configuration

Key configuration options in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.datasource.url` | localhost:3306 | MySQL connection URL |
| `spring.datasource.username` | root | MySQL username |
| `spring.datasource.password` | root | MySQL password |
| `app.jwt.secret` | (configured) | JWT signing secret key |
| `app.jwt.expiration-ms` | 86400000 | Token expiry (24 hours) |
| `spring.jpa.hibernate.ddl-auto` | update | Auto-create/update tables |
| `spring.servlet.multipart.max-file-size` | 10MB | Max file upload size |

## 🌐 Frontend Integration

The backend is designed to work with any frontend (React, HTML/CSS/JS, etc):

1. **CORS** is enabled for all origins (restrict in production)
2. **Consistent JSON response format** via `ApiResponse` wrapper
3. **JWT token** in Authorization header for authenticated requests
4. **RESTful endpoints** following standard conventions

### Frontend Connection Example (JavaScript)

```javascript
// Login
const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'student@example.com', password: 'password123' })
});
const data = await response.json();
const token = data.data.token;

// Authenticated request
const profile = await fetch('http://localhost:8080/api/profile', {
    headers: { 'Authorization': `Bearer ${token}` }
});
```

## 📝 Production Deployment Notes

1. **Change JWT secret** to a strong, unique value (use environment variables)
2. **Restrict CORS origins** to your frontend domain only
3. **Change `ddl-auto`** to `validate` and use Flyway/Liquibase for migrations
4. **Configure proper SMTP** for contact form email notifications
5. **Use HTTPS** in production
6. **Set proper MySQL credentials** using environment variables
7. **Enable Spring Actuator** for production monitoring